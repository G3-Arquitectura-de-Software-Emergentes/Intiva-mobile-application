package com.resolum.intiva.features.agent.data

import java.math.BigDecimal
import javax.inject.Inject
import retrofit2.Retrofit
import retrofit2.http.*

data class SuggestRequest(val description: String, val amount: BigDecimal, val currency: String = "PEN")
data class Suggestion(val status: String, val categoryId: Long?, val label: String?)
data class GeneratePlan(val month: String)
data class EditPlan(val version: Int, val proposedLimit: BigDecimal)
data class ApprovePlan(val version: Int)
data class PlanAction(
    val id: String, val type: String, val categoryId: Long, val label: String,
    val currentLimit: BigDecimal?, val projectedExpense: BigDecimal, val proposedLimit: BigDecimal,
    val currency: String, val status: String
)
data class MonthlyPlan(
    val id: String, val version: Int, val month: String, val status: String,
    val explanation: String, val actions: List<PlanAction>
)
data class FinancialInsights(val period: String, val smallExpenseCount: Int,
    val smallExpenseTotal: BigDecimal, val note: String)
data class PlanNotice(val id: String, val planId: String, val message: String)

interface AgentApi {
    @POST("category-suggestions") suspend fun suggest(@Body data: SuggestRequest): Suggestion
    @GET("financial-insights") suspend fun insights(@Query("month") month: String): FinancialInsights
    @POST("monthly-plans") suspend fun generate(@Body data: GeneratePlan): MonthlyPlan
    @PATCH("monthly-plans/{id}") suspend fun edit(@Path("id") id: String, @Body data: EditPlan): MonthlyPlan
    @POST("monthly-plans/{id}/approve") suspend fun approve(@Path("id") id: String, @Body data: ApprovePlan): MonthlyPlan
    @GET("monthly-plans/{id}") suspend fun get(@Path("id") id: String): MonthlyPlan
    @GET("plan-notifications") suspend fun notices(): List<PlanNotice>
}

class AgentRepository @Inject constructor(retrofit: Retrofit) {
    val api: AgentApi = retrofit.create(AgentApi::class.java)
}
