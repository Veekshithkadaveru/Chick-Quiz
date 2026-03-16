package app.krafted.chickquiz.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.krafted.chickquiz.ui.AnswerRevealScreen
import app.krafted.chickquiz.ui.CollectionScreen
import app.krafted.chickquiz.ui.HomeScreen
import app.krafted.chickquiz.ui.LeaderboardScreen
import app.krafted.chickquiz.ui.QuizScreen
import app.krafted.chickquiz.ui.ResultScreen
import app.krafted.chickquiz.ui.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Quiz : Screen("quiz/{category}/{isDaily}") {
        fun createRoute(category: String, isDaily: Boolean) = "quiz/$category/$isDaily"
    }
    object AnswerReveal : Screen("answer_reveal/{category}/{isDaily}") {
        fun createRoute(category: String, isDaily: Boolean) = "answer_reveal/$category/$isDaily"
    }
    object Result : Screen("result/{category}/{isDaily}") {
        fun createRoute(category: String, isDaily: Boolean) = "result/$category/$isDaily"
    }
    object Collection : Screen("collection")
    object Leaderboard : Screen("leaderboard/{category}") {
        fun createRoute(category: String) = "leaderboard/$category"
    }
}

@Composable
fun ChickQuizNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(Screen.Quiz.createRoute(category, false))
                },
                onDailyClick = {
                    navController.navigate(Screen.Quiz.createRoute("DAILY", true))
                },
                onCollectionClick = {
                    navController.navigate(Screen.Collection.route)
                },
                onLeaderboardClick = { category ->
                    navController.navigate(Screen.Leaderboard.createRoute(category))
                }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("isDaily") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            val isDaily = backStackEntry.arguments?.getBoolean("isDaily") ?: false
            QuizScreen(
                category = category,
                isDaily = isDaily,
                onAnswerRevealed = {
                    navController.navigate(Screen.AnswerReveal.createRoute(category, isDaily))
                },
                onSessionComplete = {
                    navController.navigate(Screen.Result.createRoute(category, isDaily)) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AnswerReveal.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("isDaily") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            val isDaily = backStackEntry.arguments?.getBoolean("isDaily") ?: false
            AnswerRevealScreen(
                onNext = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("isDaily") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            val isDaily = backStackEntry.arguments?.getBoolean("isDaily") ?: false
            ResultScreen(
                category = category,
                isDaily = isDaily,
                onPlayAgain = {
                    navController.navigate(Screen.Quiz.createRoute(category, isDaily)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Collection.route) {
            CollectionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Leaderboard.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            LeaderboardScreen(
                initialCategory = category,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
