package com.ocram.qichwadic.features.common.data.remote

import com.ocram.qichwadic.features.common.data.model.DefinitionEntity
import com.ocram.qichwadic.features.common.data.model.DictionaryEntity
import com.ocram.qichwadic.features.common.data.model.SearchResultEntity
import retrofit2.Response

interface Service {

    suspend fun getDictionaries(): Response<List<DictionaryEntity>>

    suspend fun searchWord(searchWord: String, fromQuechua: Int, target: String, searchType: Int): Response<List<SearchResultEntity>>

    suspend fun fetchMoreResults(dictionaryId: Int, word: String, fromQuechua: Int, searchType: Int, page: Int): Response<SearchResultEntity>

    suspend fun getAllDefinitionsByDictionary(dictionaryId: Int): Response<List<DefinitionEntity>>
}
