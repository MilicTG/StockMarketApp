package com.delminiusdevs.stockmarketapp.domain.repository

import com.delminiusdevs.stockmarketapp.domain.model.CompanyListing
import com.delminiusdevs.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

}