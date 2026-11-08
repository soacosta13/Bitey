package com.example.bitey.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bitey.databinding.FragmentSavedMealsBinding
import com.example.bitey.ui.adapter.MealAdapter
import com.example.bitey.ui.viewmodel.MainViewModel
import com.example.bitey.R
import com.example.bitey.data.model.MealSummary


/*displays recipes stored in SQLite*/
class SavedMealsFragment : Fragment() {

    private var _binding: FragmentSavedMealsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()


    private val savedAdapter = MealAdapter(
        onFavoriteClick = { meal ->
            viewModel.toggleFavorite(meal)
        },
        onMealClick = { meal ->
            openDetail(meal)
        }
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedMealsBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    /*opens a saved recipe to be read offline*/
    private fun openDetail(meal: MealSummary) {
        viewModel.selectMeal(meal)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MealDetailFragment())
            .addToBackStack(null)
            .commit()
    }


    private fun setupRecyclerView() {
        binding.recyclerViewSaved.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerViewSaved.adapter = savedAdapter
    }

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            savedAdapter.submitList(favorites)

            val isEmpty = favorites.isEmpty()
            binding.tvEmptySaved.isVisible = isEmpty
            binding.recyclerViewSaved.isVisible = !isEmpty
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null && !isHidden) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.onToastShown()
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerViewSaved.adapter = null
        _binding = null
        super.onDestroyView()
    }

}
