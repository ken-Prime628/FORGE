package com.kennedy.forge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.screens.auth.LoginScreen
import com.kennedy.forge.ui.screens.auth.RegisterScreen
import com.kennedy.forge.ui.screens.community.CollaborationScreen
import com.kennedy.forge.ui.screens.community.DiscoveryFeedScreen
import com.kennedy.forge.ui.screens.dashboard.DashboardScreen
import com.kennedy.forge.ui.screens.feedback.EditProjectScreen
import com.kennedy.forge.ui.screens.feedback.FeedbackDashboardScreen
import com.kennedy.forge.ui.screens.feedback.SubmitWorkScreen
import com.kennedy.forge.ui.screens.feedback.ProjectDetailScreen
import com.kennedy.forge.ui.screens.onboarding.OnboardingScreen1
import com.kennedy.forge.ui.screens.onboarding.OnboardingScreen2
import com.kennedy.forge.ui.screens.onboarding.OnboardingScreen3
import com.kennedy.forge.ui.screens.onboarding.ProfileSetupScreen
import com.kennedy.forge.ui.screens.onboarding.SkillAssessmentScreen
import com.kennedy.forge.ui.screens.profile.ProfileScreen
import com.kennedy.forge.ui.screens.splash.SplashScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }

        composable(ROUTE_Login) {
            LoginScreen(navController)
        }
        composable(ROUTE_Register) {
            RegisterScreen(navController)
        }







        composable(ROUT_ONBOARDING1) {
                OnboardingScreen1(navController)
            }
        composable(ROUT_ONBOARDING2) {
                OnboardingScreen2(navController)
            }
        composable(ROUT_ONBOARDING3) {
                OnboardingScreen3(navController)
            }
        composable(ROUT_ProfileSetup) {
            ProfileSetupScreen(navController)
        }
        composable(ROUT_SkillAssessment) {
            SkillAssessmentScreen(navController)
        }
        composable(ROUT_Dashboard) {
            DashboardScreen(navController)
        }
        composable(ROUT_SubmitWork) {
            SubmitWorkScreen(navController)
        }
        composable(ROUT_FeedbackDashboard) {
            FeedbackDashboardScreen(navController)
        }
        composable(ROUT_ProjectDetail) {
            ProjectDetailScreen(navController)
        }
        composable(ROUT_Profile) {
            ProfileScreen(navController)
        }
        composable(ROUT_DiscoveryFeed) {
            DiscoveryFeedScreen(navController)
        }

        composable(ROUT_EditProject) {
            EditProjectScreen(navController)
        }
        composable(ROUT_Collaboration) {
            CollaborationScreen(navController)
        }



    }
}