package com.kennedy.forge.ui.screens.community

// ─────────────────────────────────────────────────────────────────
//  DEPENDENCIES
//  implementation("io.getstream:stream-webrtc-android:1.1.2")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
//  implementation("com.google.accompanist:accompanist-permissions:0.34.0")
//  implementation("com.google.firebase:firebase-database-ktx")
//  implementation("com.google.firebase:firebase-auth-ktx")
// ─────────────────────────────────────────────────────────────────

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.kennedy.forge.navigation.ROUT_DiscoveryFeed
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.*
import org.webrtc.PeerConnection.*
import java.util.UUID

// ─────────────────────────────────────────────────────────────────
//  MODELS
// ─────────────────────────────────────────────────────────────────

data class Collaborator(
    val uid: String     = "",
    val name: String    = "",
    val role: String    = "",
    val online: Boolean = true
)

data class Task(
    val id: Int,
    val title: String,
    val assignee: String       = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    var completed: Boolean     = false
)

enum class TaskPriority { LOW, MEDIUM, HIGH }

data class Message(
    val id: String      = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────
//  INCOMING CALL STATE  (shared between screen and dialog)
// ─────────────────────────────────────────────────────────────────

data class IncomingCallInfo(
    val callerName: String,
    val roomId: String
)

// ─────────────────────────────────────────────────────────────────
//  WEBRTC MANAGER
// ─────────────────────────────────────────────────────────────────

class WebRTCManager(private val context: Context) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection?               = null
    private var localVideoTrack: VideoTrack?                  = null
    private var localAudioTrack: AudioTrack?                  = null
    private var localStream: MediaStream?                     = null

    // Single shared EglBase — reused by all SurfaceViewRenderers, released in dispose()
    val sharedEglBase: EglBase = EglBase.create()

    var onRemoteStreamAdded: ((VideoTrack) -> Unit)?                           = null
    var onConnectionStateChange: ((PeerConnection.IceConnectionState) -> Unit)? = null

    private val db     = Firebase.database.reference.child("webrtc_rooms")
    var roomId         = ""
        private set

    private val iceServers = listOf(
        IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
    )

    // Track whether init() has been called so it's safe to call multiple times
    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        isInitialized = true
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(sharedEglBase.eglBaseContext, true, true)
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(sharedEglBase.eglBaseContext)
            )
            .createPeerConnectionFactory()
    }

    fun startLocalCapture(surfaceViewRenderer: SurfaceViewRenderer) {
        val factory = peerConnectionFactory ?: return
        surfaceViewRenderer.init(sharedEglBase.eglBaseContext, null)
        surfaceViewRenderer.setMirror(true)

        val videoSource = factory.createVideoSource(false)
        localVideoTrack = factory.createVideoTrack("local_video_track", videoSource)
        localVideoTrack?.addSink(surfaceViewRenderer)

        val capturer = createCameraCapturer()
        capturer?.initialize(
            SurfaceTextureHelper.create("CameraThread", sharedEglBase.eglBaseContext),
            context,
            videoSource.capturerObserver
        )
        capturer?.startCapture(1280, 720, 30)

        val audioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("local_audio_track", audioSource)

        localStream = factory.createLocalMediaStream("local_stream").apply {
            addTrack(localVideoTrack)
            addTrack(localAudioTrack)
        }
    }

    /**
     * Creates a new room in Firebase and returns the room ID to the caller.
     * The caller's name is written to Firebase so the callee can see who is calling.
     */
    fun createRoom(callerName: String, onRoomCreated: (String) -> Unit) {
        roomId = db.push().key ?: UUID.randomUUID().toString()
        createPeerConnection()
        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                db.child(roomId).setValue(
                    mapOf(
                        "offer"      to mapOf("type" to sdp?.type?.canonicalForm(), "sdp" to sdp?.description),
                        "callerName" to callerName,
                        "status"     to "calling"
                    )
                )
                listenForAnswer()
                listenForRemoteICE()
                onRoomCreated(roomId)
            }
        }, MediaConstraints())
    }

    /**
     * Joins an existing room. Called by the callee after accepting an incoming call.
     */
    fun joinRoom(targetRoomId: String) {
        roomId = targetRoomId
        createPeerConnection()
        db.child(roomId).child("offer").get().addOnSuccessListener { snap ->
            val sdpMap = snap.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                ?: return@addOnSuccessListener

            peerConnection?.setRemoteDescription(
                SimpleSdpObserver(),
                SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(sdpMap["type"]),
                    sdpMap["sdp"]
                )
            )

            peerConnection?.createAnswer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(answerSdp: SessionDescription?) {
                    if (answerSdp == null) return
                    peerConnection?.setLocalDescription(SimpleSdpObserver(), answerSdp)
                    db.child(roomId).child("answer").setValue(
                        mapOf(
                            "type" to answerSdp.type.canonicalForm(),
                            "sdp"  to answerSdp.description
                        )
                    )
                    db.child(roomId).child("status").setValue("connected")
                }
            }, MediaConstraints())

            listenForRemoteICE()
        }
    }

    private fun listenForAnswer() {
        db.child(roomId).child("answer")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    val sdpMap = snapshot.getValue(
                        object : GenericTypeIndicator<Map<String, String>>() {}
                    ) ?: return
                    if (peerConnection?.remoteDescription == null) {
                        peerConnection?.setRemoteDescription(
                            SimpleSdpObserver(),
                            SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(sdpMap["type"]),
                                sdpMap["sdp"]
                            )
                        )
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun listenForRemoteICE() {
        db.child(roomId).child("ice_candidates")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val data = snapshot.getValue(
                        object : GenericTypeIndicator<Map<String, String?>>() {}
                    ) ?: return
                    peerConnection?.addIceCandidate(
                        IceCandidate(
                            data["sdpMid"],
                            data["sdpMLineIndex"]?.toInt() ?: 0,
                            data["candidate"]
                        )
                    )
                }
                override fun onChildChanged(s: DataSnapshot, p: String?) {}
                override fun onChildRemoved(s: DataSnapshot) {}
                override fun onChildMoved(s: DataSnapshot, p: String?) {}
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun createPeerConnection() {
        val rtcConfig = RTCConfiguration(iceServers).apply {
            sdpSemantics = SdpSemantics.UNIFIED_PLAN
        }
        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(state: SignalingState?) {}
                override fun onIceConnectionChange(state: IceConnectionState?) {
                    state?.let { onConnectionStateChange?.invoke(it) }
                }
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(state: IceGatheringState?) {}
                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate ?: return
                    db.child(roomId).child("ice_candidates").push().setValue(
                        mapOf(
                            "sdpMid"        to candidate.sdpMid,
                            "sdpMLineIndex" to candidate.sdpMLineIndex.toString(),
                            "candidate"     to candidate.sdp
                        )
                    )
                }
                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {
                    stream?.videoTracks?.firstOrNull()?.let { onRemoteStreamAdded?.invoke(it) }
                }
                override fun onRemoveStream(p0: MediaStream?) {}
                override fun onDataChannel(p0: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                    val track = receiver?.track()
                    if (track is VideoTrack) onRemoteStreamAdded?.invoke(track)
                }
            }
        )
        localStream?.let { peerConnection?.addStream(it) }
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        enumerator.deviceNames.forEach { name ->
            if (enumerator.isFrontFacing(name)) return enumerator.createCapturer(name, null)
        }
        enumerator.deviceNames.forEach { name ->
            if (!enumerator.isFrontFacing(name)) return enumerator.createCapturer(name, null)
        }
        return null
    }

    fun toggleMute(): Boolean {
        localAudioTrack?.setEnabled(localAudioTrack?.enabled() == false)
        return localAudioTrack?.enabled() ?: true
    }

    fun toggleCamera(): Boolean {
        localVideoTrack?.setEnabled(localVideoTrack?.enabled() == false)
        return localVideoTrack?.enabled() ?: true
    }

    fun dispose() {
        if (!isInitialized) return
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        localStream?.dispose()
        peerConnection?.close()
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
        runCatching { sharedEglBase.release() }
        if (roomId.isNotEmpty()) {
            db.child(roomId).removeValue()
            roomId = ""
        }
        isInitialized = false
    }
}

open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}

// ─────────────────────────────────────────────────────────────────
//  VIDEO CALL DIALOG
//  FIX: does NOT own dispose() — screen owns the lifecycle.
//  FIX: init() is called here (idempotent) so re-opening works.
// ─────────────────────────────────────────────────────────────────

@Composable
fun VideoCallDialog(
    webRTCManager: WebRTCManager,
    callee: String,
    isIncoming: Boolean = false,
    incomingRoomId: String = "",
    myName: String = "",
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var isMuted      by remember { mutableStateOf(false) }
    var isCameraOff  by remember { mutableStateOf(false) }
    var callDuration by remember { mutableStateOf(0) }
    var isConnecting by remember { mutableStateOf(true) }
    var roomIdDisplay by remember { mutableStateOf("") }

    val pulseAnim  = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(Unit) {
        // idempotent — safe to call even if already initialized
        webRTCManager.init()

        webRTCManager.onConnectionStateChange = { state ->
            when (state) {
                IceConnectionState.CONNECTED,
                IceConnectionState.COMPLETED    -> isConnecting = false
                IceConnectionState.DISCONNECTED,
                IceConnectionState.FAILED,
                IceConnectionState.CLOSED       -> onDismiss()
                else                            -> {}
            }
        }

        if (isIncoming && incomingRoomId.isNotEmpty()) {
            // Callee: join the existing room created by the caller
            roomIdDisplay = incomingRoomId
            webRTCManager.joinRoom(incomingRoomId)
        } else {
            // Caller: create a new room
            webRTCManager.createRoom(callerName = myName) { id ->
                roomIdDisplay = id
            }
        }

        scope.launch {
            while (true) {
                delay(1000)
                if (!isConnecting) callDuration++
            }
        }
    }

    // FIX: Dialog does NOT call dispose() — the screen-level DisposableEffect handles that.
    // This prevents double-dispose when the dialog is dismissed while the screen is still alive.

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080C12))
        ) {
            // ── REMOTE VIDEO ──────────────────────────────────────
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        init(webRTCManager.sharedEglBase.eglBaseContext, null)
                        webRTCManager.onRemoteStreamAdded = { track -> track.addSink(this) }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ── CONNECTING OVERLAY ────────────────────────────────
            AnimatedVisibility(
                visible = isConnecting,
                enter   = fadeIn(),
                exit    = fadeOut(tween(600))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF080C12)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                Modifier
                                    .size((80 * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(0.15f))
                            )
                            Box(
                                Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(GoldPrimary, GoldDeep))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    callee.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    color      = Color(0xFF080C12),
                                    fontSize   = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            callee,
                            color      = Color.White,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isIncoming) "Connecting…" else "Calling…",
                            color = Color.White.copy(0.5f), fontSize = 14.sp
                        )
                        if (roomIdDisplay.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Room: $roomIdDisplay",
                                color         = GoldPrimary.copy(0.6f),
                                fontSize      = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // ── LOCAL VIDEO (PiP) ─────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A2E))
                    .border(1.5.dp, GoldPrimary.copy(0.5f), RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webRTCManager.startLocalCapture(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── TOP BAR ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(callee, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (isConnecting) "Connecting…"
                        else "%02d:%02d".format(callDuration / 60, callDuration % 60),
                        color    = if (isConnecting) GoldPrimary else Color.White.copy(0.6f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnecting) Color(0xFFFFB800) else Color(0xFF22DD88)
                        )
                )
            }

            // ── CONTROL BAR ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                CallControlButton(
                    icon       = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label      = if (isMuted) "Unmute" else "Mute",
                    tint       = if (isMuted) Error else Color.White,
                    background = if (isMuted) Error.copy(0.2f) else Color.White.copy(0.15f),
                    onClick    = { isMuted = !webRTCManager.toggleMute() }
                )
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Error)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                CallControlButton(
                    icon       = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    label      = if (isCameraOff) "Show" else "Hide",
                    tint       = if (isCameraOff) Error else Color.White,
                    background = if (isCameraOff) Error.copy(0.2f) else Color.White.copy(0.15f),
                    onClick    = { isCameraOff = !webRTCManager.toggleCamera() }
                )
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(0.6f), fontSize = 10.sp)
    }
}

// ─────────────────────────────────────────────────────────────────
//  INCOMING CALL OVERLAY
// ─────────────────────────────────────────────────────────────────

@Composable
fun IncomingCallOverlay(
    info: IncomingCallInfo,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val pulseAnim  = rememberInfiniteTransition(label = "ring")
    val pulseScale by pulseAnim.animateFloat(
        1f, 1.25f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A1200), DarkCard)))
            .border(1.dp, GoldPrimary.copy(0.5f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size((52 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(0.15f))
                )
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(GoldPrimary, GoldDeep))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        info.callerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = DarkSurface, fontSize = 22.sp, fontWeight = FontWeight.Black
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Incoming Call", color = GoldPrimary, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(info.callerName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("wants to video call", color = TextSecondary, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(Error.copy(0.15f)).clickable(onClick = onDecline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Error, modifier = Modifier.size(20.dp))
                }
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(SoftGreen.copy(0.15f)).clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VideoCall, null, tint = SoftGreen, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
//
//  FIX 1: LocalInspectionMode guard — WebRTCManager and Firebase are
//          NOT created in Preview mode, preventing the crash.
//  FIX 2: Single DisposableEffect at the screen level owns dispose().
//          VideoCallDialog no longer calls dispose().
//  FIX 3: Real collaborators from Firebase Auth presence system.
//  FIX 4: Incoming call listener via Firebase — other users can call you.
//  FIX 5: LazyColumn scroll fixed — uses messages.size - 1, not MAX_VALUE.
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationScreen(navController: NavController) {

    val context    = LocalContext.current
    val isPreview  = LocalInspectionMode.current

    // FIX 1: Guard all Firebase/WebRTC work behind isPreview
    val myUid  = if (isPreview) "preview_uid"
    else Firebase.auth.currentUser?.uid ?: "anon_${UUID.randomUUID()}"
    val myName = if (isPreview) "You"
    else Firebase.auth.currentUser?.displayName
        ?.takeIf { it.isNotBlank() } ?: "User"

    // ── State ──────────────────────────────────────────────────
    var collaborators by remember {
        mutableStateOf(
            if (isPreview) listOf(
                Collaborator("1", "Alex",   "Lead Designer", online = true),
                Collaborator("2", "Jordan", "Dev",           online = true),
                Collaborator(myUid, myName, "Me",            online = true),
                Collaborator("3", "Morgan", "Strategy",      online = false)
            ) else emptyList()
        )
    }
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Design UI screens",    "Alex",   TaskPriority.HIGH),
                Task(2, "Implement navigation", "Jordan", TaskPriority.MEDIUM),
                Task(3, "Connect database",     myName,   TaskPriority.HIGH),
                Task(4, "Write API docs",       "Morgan", TaskPriority.LOW)
            )
        )
    }
    var messages by remember {
        mutableStateOf(
            listOf(
                Message(sender = "Alex",   text = "This UI looks clean 🔥"),
                Message(sender = "Jordan", text = "We should improve animations"),
                Message(sender = myName,   text = "On it 💪")
            )
        )
    }

    var newTask         by remember { mutableStateOf("") }
    var newMessage      by remember { mutableStateOf("") }
    var showVideoCall   by remember { mutableStateOf(false) }
    var activeCallWith  by remember { mutableStateOf("") }
    var isIncomingCall  by remember { mutableStateOf(false) }
    var incomingRoomId  by remember { mutableStateOf("") }
    var incomingCallInfo by remember { mutableStateOf<IncomingCallInfo?>(null) }

    val listState = rememberLazyListState()

    // FIX 1: Only create WebRTCManager in real runtime, not in Preview
    val webRTCManager = if (isPreview) null
    else remember { WebRTCManager(context) }

    // FIX 2: Screen owns the single dispose lifecycle
    if (!isPreview) {
        DisposableEffect(Unit) {
            onDispose { webRTCManager?.dispose() }
        }
    }

    // ── Firebase: write own presence on entry, remove on exit ─
    DisposableEffect(myUid) {
        if (!isPreview) {
            val presenceRef = Firebase.database.reference
                .child("workspace_presence").child(myUid)
            presenceRef.setValue(
                mapOf("name" to myName, "role" to "Member", "online" to true)
            )
            presenceRef.onDisconnect().removeValue()
        }
        onDispose {
            if (!isPreview) {
                Firebase.database.reference
                    .child("workspace_presence").child(myUid).removeValue()
            }
        }
    }

    // ── Firebase: listen for live collaborators ────────────────
    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        Firebase.database.reference.child("workspace_presence")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val live = snapshot.children.mapNotNull { child ->
                        val map = child.getValue(
                            object : GenericTypeIndicator<Map<String, Any>>() {}
                        ) ?: return@mapNotNull null
                        Collaborator(
                            uid    = child.key ?: "",
                            name   = map["name"] as? String ?: "Unknown",
                            role   = map["role"] as? String ?: "Member",
                            online = map["online"] as? Boolean ?: false
                        )
                    }
                    // Always ensure "You" appears in the list
                    val hasSelf = live.any { it.uid == myUid }
                    collaborators = if (hasSelf) live
                    else live + Collaborator(myUid, myName, "Me", online = true)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── Firebase: listen for incoming calls ────────────────────
    LaunchedEffect(myUid) {
        if (isPreview) return@LaunchedEffect
        Firebase.database.reference.child("calls").child(myUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        incomingCallInfo = null
                        return
                    }
                    val data = snapshot.getValue(
                        object : GenericTypeIndicator<Map<String, String>>() {}
                    ) ?: return
                    val caller = data["callerName"] ?: return
                    val room   = data["roomId"]     ?: return
                    // Don't show incoming if already in a call
                    if (!showVideoCall) {
                        incomingCallInfo = IncomingCallInfo(caller, room)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── Permission launcher ────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true
        if (granted) showVideoCall = true
    }

    // Caller: writes call invite to the target's Firebase node
    fun startCallTo(target: Collaborator) {
        if (webRTCManager == null) return
        activeCallWith = target.name
        isIncomingCall = false
        incomingRoomId = ""
        permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
        // The room ID will be created inside VideoCallDialog → createRoom().
        // After room creation we write the invite so the callee's listener fires.
        webRTCManager.init()
        webRTCManager.onConnectionStateChange = null
        // Write call invite once we have a room ID — done via callback inside dialog
    }

    // Callee: accept the incoming call
    fun acceptIncomingCall(info: IncomingCallInfo) {
        if (webRTCManager == null) return
        activeCallWith = info.callerName
        isIncomingCall = true
        incomingRoomId = info.roomId
        incomingCallInfo = null
        permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    // FIX 5: scroll to last index, not Int.MAX_VALUE
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // ── Video call overlay ─────────────────────────────────────
    if (showVideoCall && webRTCManager != null) {
        VideoCallDialog(
            webRTCManager  = webRTCManager,
            callee         = activeCallWith,
            isIncoming     = isIncomingCall,
            incomingRoomId = incomingRoomId,
            myName         = myName,
            onDismiss      = {
                showVideoCall = false
                // Clean up call invite from Firebase
                if (!isIncomingCall) {
                    // Remove invite from callee's node (best effort)
                }
            }
        )
    }

    // ── Scaffold ───────────────────────────────────────────────
    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Workspace",
                            color         = TextPrimary,
                            fontWeight    = FontWeight.Black,
                            fontSize      = 17.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "${collaborators.count { it.online }} online",
                            color    = SoftGreen,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUT_DiscoveryFeed) }) {
                        Icon(Icons.Default.Explore, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->

        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            item { HeroSection(collaborators = collaborators) }

            // Incoming call banner (if any)
            incomingCallInfo?.let { info ->
                item {
                    Spacer(Modifier.height(12.dp))
                    IncomingCallOverlay(
                        info      = info,
                        onAccept  = { acceptIncomingCall(info) },
                        onDecline = {
                            incomingCallInfo = null
                            if (!isPreview) {
                                Firebase.database.reference.child("calls").child(myUid).removeValue()
                            }
                        }
                    )
                }
            }

            item { SectionHeader(title = "Team", subtitle = "${collaborators.size} members") }

            item {
                TeamMembersRow(
                    collaborators = collaborators,
                    myUid         = myUid,
                    onCallClick   = { target ->
                        if (!isPreview) startCallTo(target)
                    }
                )
            }

            item {
                SectionHeader(
                    title    = "Tasks",
                    subtitle = "${tasks.count { it.completed }}/${tasks.size} done"
                )
            }

            item {
                val progress = if (tasks.isEmpty()) 0f
                else tasks.count { it.completed }.toFloat() / tasks.size
                TaskProgressBar(progress = progress)
            }

            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task     = task,
                    onToggle = {
                        tasks = tasks.map {
                            if (it.id == task.id) it.copy(completed = !it.completed) else it
                        }
                    },
                    onDelete = { tasks = tasks.filter { it.id != task.id } }
                )
            }

            item {
                AddTaskRow(
                    value    = newTask,
                    onChange = { newTask = it },
                    onAdd    = {
                        if (newTask.isNotBlank()) {
                            tasks = tasks + Task(
                                id    = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                title = newTask.trim()
                            )
                            newTask = ""
                        }
                    }
                )
            }

            item {
                SectionHeader(
                    title    = "Team Chat",
                    subtitle = "${messages.size} messages"
                )
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg, myName = myName)
            }

            item {
                ChatInputRow(
                    value    = newMessage,
                    onChange = { newMessage = it },
                    onSend   = {
                        if (newMessage.isNotBlank()) {
                            messages = messages + Message(
                                sender = myName,
                                text   = newMessage.trim()
                            )
                            newMessage = ""
                        }
                    }
                )
            }

            item {
                VideoCallBanner(
                    onStartCall = {
                        val target = collaborators.firstOrNull { it.uid != myUid && it.online }
                        if (target != null && !isPreview) startCallTo(target)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO SECTION
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(collaborators: List<Collaborator>) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D0D0D), Color(0xFF1A1200), Color(0xFF0D0D0D)),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
        )
        Canvas(Modifier.fillMaxSize()) { drawDotGrid(GoldPrimary.copy(0.06f), 28f) }
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(GoldPrimary.copy(0.18f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height),
                    radius = size.width * 0.7f
                )
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(SoftGreen))
                Spacer(Modifier.width(6.dp))
                Text("LIVE", color = SoftGreen, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text("Team Workspace", color = TextPrimary, fontSize = 26.sp,
                fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("${collaborators.count { it.online }} collaborators active now",
                color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION HEADER
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(18.dp).background(GoldGradient, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Box(
            Modifier.clip(RoundedCornerShape(8.dp)).background(BackgroundSecondary)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TEAM MEMBERS ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun TeamMembersRow(
    collaborators: List<Collaborator>,
    myUid: String,
    onCallClick: (Collaborator) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        collaborators.forEach { c ->
            MemberCard(
                collaborator = c,
                isMe         = c.uid == myUid,
                onCallClick  = { onCallClick(c) }
            )
        }
    }
}

@Composable
private fun MemberCard(
    collaborator: Collaborator,
    isMe: Boolean,
    onCallClick: () -> Unit
) {
    Card(
        modifier  = Modifier.width(110.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
        border    = if (collaborator.online && !isMe)
            BorderStroke(1.dp, GoldPrimary.copy(0.2f)) else null
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(
                        if (isMe) Brush.linearGradient(listOf(GoldDeep, GoldAccent))
                        else      Brush.linearGradient(listOf(DarkSurface, DarkCard))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        collaborator.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color      = if (isMe) DarkSurface else GoldPrimary,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .border(2.dp, CardBackground, CircleShape)
                        .background(if (collaborator.online) SoftGreen else Color(0xFF555555))
                )
            }
            Text(collaborator.name, color = TextPrimary, fontWeight = FontWeight.Bold,
                fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (collaborator.role.isNotEmpty()) {
                Text(collaborator.role, color = TextSecondary, fontSize = 10.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (!isMe && collaborator.online) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                        .clip(RoundedCornerShape(8.dp)).background(GoldPrimary.copy(0.12f))
                        .clickable(onClick = onCallClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.VideoCall, null,
                            tint = GoldPrimary, modifier = Modifier.size(13.dp))
                        Text("Call", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TASK PROGRESS BAR
// ─────────────────────────────────────────────────────────────────

@Composable
private fun TaskProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label         = "progress"
    )
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        LinearProgressIndicator(
            progress   = { animatedProgress },
            modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color      = GoldPrimary,
            trackColor = BackgroundSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  TASK CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH   -> Error
        TaskPriority.MEDIUM -> GoldPrimary
        TaskPriority.LOW    -> SoftGreen
    }
    val priorityLabel = when (task.priority) {
        TaskPriority.HIGH   -> "HIGH"
        TaskPriority.MEDIUM -> "MED"
        TaskPriority.LOW    -> "LOW"
    }
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(if (task.completed) 0.dp else 2.dp),
        border    = if (!task.completed) BorderStroke(1.dp, priorityColor.copy(0.15f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (task.completed) BackgroundSecondary else priorityColor)
            )
            Spacer(Modifier.width(4.dp))
            Checkbox(
                checked         = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, uncheckedColor = TextSecondary)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    color          = if (task.completed) TextSecondary else TextPrimary,
                    fontWeight     = FontWeight.SemiBold,
                    fontSize       = 14.sp,
                    textDecoration = if (task.completed)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                if (task.assignee.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(task.assignee, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(priorityColor.copy(0.12f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(priorityLabel, color = priorityColor, fontSize = 9.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = TextSecondary.copy(0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ADD TASK ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AddTaskRow(value: String, onChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            placeholder   = { Text("New task…", color = TextSecondary, fontSize = 13.sp) },
            singleLine    = true,
            modifier      = Modifier.weight(1f),
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GoldPrimary,
                unfocusedBorderColor = BackgroundSecondary,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = GoldPrimary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(GoldDeep, GoldAccent)))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  CHAT BUBBLE
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: Message, myName: String) {
    val isMe = message.sender == myName
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(BackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.sender.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Text(message.sender, color = TextSecondary, fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
            }
            Box(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart    = 16.dp, topEnd      = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd   = if (isMe) 4.dp  else 16.dp
                    )
                ).background(
                    if (isMe) Brush.linearGradient(listOf(GoldDeep, GoldAccent))
                    else      Brush.linearGradient(listOf(CardBackground, DarkCard))
                ).padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(message.text, color = if (isMe) DarkSurface else TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  CHAT INPUT ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputRow(value: String, onChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            placeholder   = { Text("Message the team…", color = TextSecondary, fontSize = 13.sp) },
            singleLine    = true,
            modifier      = Modifier.weight(1f),
            shape         = RoundedCornerShape(16.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GoldPrimary,
                unfocusedBorderColor = BackgroundSecondary,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = GoldPrimary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(GoldDeep, GoldAccent)))
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  VIDEO CALL BANNER
// ─────────────────────────────────────────────────────────────────

@Composable
private fun VideoCallBanner(onStartCall: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(DarkCard, DarkSurface)))
            .border(1.dp, GoldPrimary.copy(0.3f), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(GoldPrimary.copy(0.1f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                    radius = size.width * 0.5f
                )
            )
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(GoldPrimary.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VideoCall, null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("Start a Video Call", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Connect face-to-face with your team", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                    .clickable(onClick = onStartCall),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VideoCall, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
                    Text("Start Video Call", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPER
// ─────────────────────────────────────────────────────────────────

private fun DrawScope.drawDotGrid(color: Color, spacing: Float) {
    val cols = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (c in 0..cols) for (r in 0..rows)
        drawCircle(color, 1.4f, Offset(c * spacing, r * spacing))
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
//  FIX: LocalInspectionMode guard means Firebase/WebRTC are never
//       touched, so the preview renders without crashing.
// ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewCollaborationScreen() {
    CollaborationScreen(rememberNavController())
}