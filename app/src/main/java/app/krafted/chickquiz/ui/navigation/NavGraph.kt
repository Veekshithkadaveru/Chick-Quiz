package app.krafted.chickquiz.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.krafted.chickquiz.ui.CollectionScreen
import app.krafted.chickquiz.ui.HomeScreen
import app.krafted.chickquiz.ui.LeaderboardScreen
import app.krafted.chickquiz.ui.QuizScreen
import app.krafted.chickquiz.ui.ResultScreen
import app.krafted.chickquiz.ui.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Quiz : Screen("quiz/{category}") {
        fun createRoute(category: String) = "quiz/$category"
    }
    object Result : Screen("result/{category}?score={score}&correctCount={correctCount}&isPersonalBest={isPersonalBest}&starsEarned={starsEarned}&newUnlocks={newUnlocks}") {
        fun createRoute(category: String, score: Int, correctCount: Int, isPersonalBest: Boolean, starsEarned: Int, newUnlocks: String) =
            "result/$category?score=$score&correctCount=$correctCount&isPersonalBest=$isPersonalBest&starsEarned=$starsEarned&newUnlocks=${Uri.encode(newUnlocks)}"
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
                    navController.navigate(Screen.Quiz.createRoute(category))
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
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            QuizScreen(
                category = category,
                onSessionComplete = { score, correctCount, isPersonalBest, starsEarned, newUnlocks ->
                    navController.navigate(
                        Screen.Result.createRoute(category, score, correctCount, isPersonalBest, starsEarned, newUnlocks)
                    ) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType; defaultValue = 0 },
                navArgument("correctCount") { type = NavType.IntType; defaultValue = 0 },
                navArgument("isPersonalBest") { type = NavType.BoolType; defaultValue = false },
                navArgument("starsEarned") { type = NavType.IntType; defaultValue = 0 },
                navArgument("newUnlocks") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "BREEDS"
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val correctCount = backStackEntry.arguments?.getInt("correctCount") ?: 0
            val isPersonalBest = backStackEntry.arguments?.getBoolean("isPersonalBest", false) ?: false
            val starsEarned = backStackEntry.arguments?.getInt("starsEarned") ?: 0
            val newUnlocks = backStackEntry.arguments?.getString("newUnlocks")
                ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            ResultScreen(
                category = category,
                score = score,
                correctCount = correctCount,
                isPersonalBest = isPersonalBest,
                starsEarned = starsEarned,
                newUnlocks = newUnlocks,
                onPlayAgain = {
                    navController.navigate(Screen.Quiz.createRoute(category)) {
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
