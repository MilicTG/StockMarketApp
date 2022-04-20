package com.delminiusdevs.stockmarketapp.data.repository

import com.delminiusdevs.stockmarketapp.data.local.StockDatabase
import com.delminiusdevs.stockmarketapp.data.mapper.toCompanyListing
import com.delminiusdevs.stockmarketapp.data.remote.StockApi
import com.delminiusdevs.stockmarketapp.domain.model.CompanyListing
import com.delminiusdevs.stockmarketapp.domain.repository.StockRepository
import com.delminiusdevs.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db: StockDatabase
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            val localListings = dao.searchCompanyListing(query = query)
            emit(
                Resource.Success(
                    data = localListings.map {
                        it.toCompanyListing()
                    }
                )
            )

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(isLoading = false))
                return@flow
            }

            //Get CSV file
            val remoteListing = try {
                val response = api.getListings()
                response.byteStream()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        message = "Couldn't load data"
                    )
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        message = "Couldn't load data"
                    )
                )
            }
        }
    }
}