package com.example.bitey.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bitey.databinding.FragmentMealListBinding
import com.example.bitey.ui.adapter.MealAdapter
import com.example.bitey.ui.viewmodel.MainViewModel
import android.widget.Toast
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.example.bitey.R
import com.example.bitey.data.model.MealSummary



/*shows categories and meals*/
class MealListFragment : Fragment()
{
    private var _binding: FragmentMealListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val mealAdapter = MealAdapter(
        onFavoriteClick = { meal ->
            viewModel.toggleFavorite(meal)
        },
        onMealClick = { meal ->
            openDetail(meal)
        }
    )

    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategorySelector()
        setupListeners()
        observeViewModel()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = mealAdapter
    }

    private fun setupCategorySelector() {
        categoryAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, mutableListOf<String>()
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategories.adapter = categoryAdapter

        binding.spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val category = categoryAdapter.getItem(position) ?: return
                if (category != viewModel.selectedCategory.value) {
                    viewModel.selectCategory(category)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupListeners() {
        binding.tvEmptyOrError.setOnClickListener {
            if (viewModel.canRetry.value == true) {
                viewModel.retry()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val names = categories.map { it.name }
            categoryAdapter.clear()
            categoryAdapter.addAll(names)
            categoryAdapter.notifyDataSetChanged()
            selectSpinnerItem(viewModel.selectedCategory.value)
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            selectSpinnerItem(category)
        }

        viewModel.meals.observe(viewLifecycleOwner) { meals ->
            mealAdapter.submitList(meals)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        viewModel.listMessage.observe(viewLifecycleOwner) { message ->
            binding.tvEmptyOrError.text = message
            binding.tvEmptyOrError.isVisible = !message.isNullOrEmpty()
        }
        /*!isHidden: avoids showing msg from a hidden fragment */
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null && !isHidden) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.onToastShown() /*cleans msg after showing*/
            }
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { searching ->
            binding.btnBackToCategories.isVisible = searching
            binding.tvCategoryLabel.isVisible = !searching
            binding.cardCategorySelector.isVisible = !searching
        }

        viewModel.canRetry.observe(viewLifecycleOwner) { canRetry ->
            binding.tvEmptyOrError.isClickable = canRetry
            binding.tvEmptyOrError.isFocusable = canRetry
        }
    }

    private fun selectSpinnerItem(category: String?) {
        if (category == null) return
        val position = (0 until categoryAdapter.count).firstOrNull {
            categoryAdapter.getItem(it) == category
        } ?: return
        if (binding.spinnerCategories.selectedItemPosition != position) {
            binding.spinnerCategories.setSelection(position)
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    /*runs the search only when the keyboard search action is pressed. */
    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text
                    ?.toString()
                    .orEmpty()

                viewModel.searchMeals(query)

                // Hide the keyboard after submitting the search.
                val inputManager = requireContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

                inputManager.hideSoftInputFromWindow(
                    binding.etSearch.windowToken,
                    0
                )

                binding.etSearch.clearFocus()
                true
            } else {
                false
            }
        }

        binding.btnBackToCategories.setOnClickListener {
            binding.etSearch.text?.clear()
            viewModel.showCategoryMeals()
        }
    }

    /*opens selected meal in detail fragment*/
    private fun openDetail(meal: MealSummary) {
        viewModel.selectMeal(meal)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MealDetailFragment())
            .addToBackStack(null)
            .commit()
    }

}
