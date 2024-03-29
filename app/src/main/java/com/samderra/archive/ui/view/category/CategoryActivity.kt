package com.samderra.archive.ui.view.category

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samderra.archive.R
import com.samderra.archive.base.BaseVmActivity
import com.samderra.archive.databinding.ActivityCategoryBinding
import com.samderra.archive.ui.BottomMenuFragment
import com.samderra.archive.ui.BottomMenuItem
import com.samderra.archive.ui.adapter.ArticleGridAdapter
import com.samderra.archive.ui.model.article.SDRArticle
import com.samderra.archive.ui.model.main.SDRCategory
import com.samderra.archive.ui.view.article.ArticleActivity
import com.samderra.archive.ui.view.main.SortOption
import com.samderra.archive.util.ext.formatHtml
import com.samderra.archive.util.observeEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryActivity() : BaseVmActivity<ActivityCategoryBinding>(
    R.layout.activity_category,
    CategoryViewModel::class.java
) {

    private val category: SDRCategory by lazy {
        intent.getSerializableExtra("category") as SDRCategory
    }

    private val articleAdapter: ArticleGridAdapter by lazy {
        ArticleGridAdapter(viewModel)
    }

    fun CategoryViewModel.setObserves() {
        articleOpenEvent.observe(lifecycleOwner, ::openArticleActivity)
        event.observeEvent(lifecycleOwner) {
            when (it) {
                CategoryActions.OPEN_SORT_OPTIONS -> this@CategoryActivity.showSortOptions()
                CategoryActions.ARTICLE_DELETE_CONFIRM -> updateArticleDeleteMode(false)
            }
        }
    }

    override fun initActivity() {
        binding.tvTitle.setOnClickListener {
            showCategoryDeleteDialog()
        }
        viewModel.setObserves()
        binding.rvArticle.adapter = articleAdapter
        initCollapsingToolbar()
    }

    fun openArticleActivity(article: SDRArticle) {
        Intent(this, ArticleActivity::class.java).run {
            putExtra("category", category)
            startActivity(this)
        }
    }

    private fun showSortOptions() {
        MaterialAlertDialogBuilder(this@CategoryActivity)
            .setTitle("정렬 순서")
            .setItems(R.array.sort_options) { _, which ->
                viewModel.changeSortOptions(SortOption.indexOf(which))
            }
            .show()
    }

    private fun showCategoryDeleteDialog() {
        val options = BottomMenuFragment.BuildOptions(
            title = getString(R.string.category_delete_title),
            items = resources
                .getStringArray(R.array.category_delete_option)
                .mapIndexed { idx, string ->
                    BottomMenuItem(
                        content = string.formatHtml(),
                        callback = {
                            when (idx) {
                                0 -> {}
                                1 -> {}
                                else -> {}
                            }
                        }
                    )
                }
        )
        BottomMenuFragment(options)
            .show(supportFragmentManager, "tag")
    }


    private fun initCollapsingToolbar() {
        toolbarTitle = ""
        viewModel.category.value = category
    }

    override fun onResume() {
        super.onResume()
        viewModel.initData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_category, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateArticleDeleteMode(value: Boolean) {
        articleAdapter.updateDeleteMode(value)
        viewModel.isDeleteMode.set(value)
        if (!value) {
            viewModel.clearClickedItems()
        }
        Log.e("asdf", "${viewModel.isDeleteMode}")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_category_delete_article -> {
                updateArticleDeleteMode(true)
            }
            R.id.menu_category_delete -> {
                showCategoryDeleteDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override val viewModel: CategoryViewModel by viewModel()
    override val toolbarId: Int = R.id.toolbar
}
