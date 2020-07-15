package com.ocram.qichwadic.features.dictionaries.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.snackbar.Snackbar
import com.ocram.qichwadic.R
import com.ocram.qichwadic.features.common.DictLang
import com.ocram.qichwadic.features.common.domain.DictionaryModel
import com.ocram.qichwadic.core.ui.activity.BaseActivity

import java.util.ArrayList

import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.activity_dictionary.*

class DictionaryActivity : BaseActivity(), AdapterView.OnItemSelectedListener, DictionaryAdapter.DefinitionDownloadListener {


    override val layoutId: Int = R.layout.activity_dictionary

    private val dictionaryViewModel: DictionaryViewModel by viewModel()
    private lateinit var dictionaryAdapter: DictionaryAdapter
    private lateinit var dictLangMap: Map<String, List<DictionaryModel>>
    private lateinit var dictLangs: List<DictLang>
    private var currentLangCode: String? = null

    override fun getToolbar(): Toolbar? {
        return mToolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dictionaryViewModel.dictionariesByLang.observe(this, Observer<Map<String, List<DictionaryModel>>> { this.onDictionaryListChanged(it) })
        dictionaryViewModel.localLoading.observe(this, Observer<Boolean> { this.onLocalDictionariesLoadingChanged(it) })
        dictionaryViewModel.dictionaryActionStatus.observe(this, Observer<DictionaryActionState> { this.onDictionaryAction(it) })
    }

    override fun initViews() {
        dictLangs = resources.getStringArray(R.array.dictLangs).map { DictLang(it) }
        setTitle(getString(R.string.nav_dictionaries))
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(true)
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        setRecyclerView()
        setSpinnerAdapters()
        spTargetLanguages.onItemSelectedListener = this@DictionaryActivity
    }

    private fun setRecyclerView() {
        dictionaryAdapter = DictionaryAdapter(ArrayList(), this)
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvDictionaries.layoutManager = linearLayoutManager
        rvDictionaries.isNestedScrollingEnabled = false
        rvDictionaries.adapter = dictionaryAdapter
    }

    private fun setSpinnerAdapters() {
        val langAdapter = ArrayAdapter(this, R.layout.item_spinner_dict_lang_white, dictLangs)
        langAdapter.setDropDownViewResource(R.layout.item_spinner_dict_lang_white)
        spTargetLanguages.adapter = langAdapter
        spTargetLanguages.setSelection(0)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
        val dictLang = spTargetLanguages.getItemAtPosition(pos) as DictLang
        this.currentLangCode = dictLang.code
        dictionaryAdapter.dictionaries = dictLangMap[dictLang.code]
        dictionaryAdapter.notifyDataSetChanged()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun downloadDefinitions(pos: Int, dictionary: DictionaryModel) {
        dictionaryViewModel.downloadDictionary(pos, dictionary)
    }

    override fun removeDictionary(pos: Int, dictionary: DictionaryModel) {
        dictionaryViewModel.removeDictionary(pos, dictionary)
    }

    private fun onDictionaryListChanged(dictionariesMap: Map<String, List<DictionaryModel>>) {
        this.dictLangMap = dictionariesMap
        if (dictionariesMap.isEmpty()) {
            showNoDictionariesMessage()
        } else {
            showDictionaries(dictionariesMap[this.currentLangCode])
        }
    }

    private fun showNoDictionariesMessage() {
        spTargetLanguages.visibility = View.GONE
        rvDictionaries.visibility = View.GONE
        tvNoDictionaries.visibility = View.VISIBLE
    }

    private fun showDictionaries(dictionaries: List<DictionaryModel>?) {
        dictionaryAdapter.dictionaries = dictionaries
        dictionaryAdapter.notifyDataSetChanged()
        spTargetLanguages.visibility = View.VISIBLE
        rvDictionaries.visibility = View.VISIBLE
        tvNoDictionaries.visibility = View.GONE
    }

    private fun onToastMessageChanged(message: String?) {
        if (!message.isNullOrEmpty()) {
            Snackbar.make(clDictionaries, message, Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun onLocalDictionariesLoadingChanged(isLoading: Boolean?) {
        if (isLoading != null) {
            pbDictionariesLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun onDictionaryAction(dictionaryActionState: DictionaryActionState) {
        dictionaryActionState.dictionary.downloading = false
        dictionaryAdapter.notifyItemChanged(dictionaryActionState.pos)

        val messageTemplate: Int?

        val isSaveAction = dictionaryActionState.dictionary.existsInLocal
        messageTemplate = if(dictionaryActionState.error) {
            if (isSaveAction) R.string.dictionary_save_error else R.string.dictionary_delete_error
        } else {
            if (isSaveAction) R.string.dictionary_save_success else R.string.dictionary_delete_success
        }
        onToastMessageChanged(getString(messageTemplate, dictionaryActionState.dictionary.name))
    }

}