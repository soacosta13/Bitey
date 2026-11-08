package com.example.bitey.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bitey.R
import com.example.bitey.data.model.toSummary
import com.example.bitey.ui.compose.MealDetailScreen
import com.example.bitey.ui.theme.BiteyTheme
import com.example.bitey.ui.viewmodel.MainViewModel

/*bridge between traditional nav and compose*/
class MealDetailFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            id = R.id.meal_detail_compose_view

            // Dispose the composition when this Fragment's view is destroyed.
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                val state by viewModel.detailState.collectAsState()

                BiteyTheme {
                    MealDetailScreen(
                        state = state,
                        onBack = {
                            parentFragmentManager.popBackStack()
                        },
                        onRetry = {
                            viewModel.retryMealDetail()
                        },
                        onFavoriteClick = { meal ->
                            viewModel.toggleFavorite(
                                meal.toSummary(),
                                meal
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.onToastShown()
            }
        }
    }
}
