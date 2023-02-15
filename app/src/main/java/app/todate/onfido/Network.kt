package app.todate.onfido

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface OnfidoService {

    @Headers("Content-Type: application/json")
    @POST("applicants")
    suspend fun createApplicant(
        @Header("Authorization") authHeader:String,
        @Body req: ApplicantReq
    ): ApplicantResp

    @Headers("Content-Type: application/json")
    @GET("applicants/{applicantId}")
    suspend fun retrieveApplicant(
        @Header("Authorization") authHeader:String,
        @Path("applicantId") applicantId: String
    ): ApplicantResp

    @POST("sdk_token")
    suspend fun createSdkToken(
        @Header("Authorization") authHeader:String,
        @Body req: TokenReq
    ): TokenResp

    @Headers("Content-Type: application/json")
    @POST("workflow_runs")
    suspend fun workflowRun(
        @Header("Authorization") authHeader:String,
        @Body req: WfReq
    ): WorkFlowRunResp
}

data class ApplicantReq(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
)

data class ApplicantResp(
    @SerializedName("id") val id: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("sandbox") val sandbox: Boolean,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
)

data class TokenReq(
    @SerializedName("applicant_id") val applicantId: String,
    @SerializedName("application_id") val appId: String,
)

data class TokenResp(
    @SerializedName("token") val token: String,
)

data class WfReq(
    @SerializedName("workflow_id") val wfId: String,
    @SerializedName("applicant_id") val applicantId: String,
)

data class WorkFlowRunResp(
    @SerializedName("id") val id: String,
    @SerializedName("workflow_id") var workflowId: String? = null,
    @SerializedName("workflow_version_id") var workflowVersionId: Int? = null,
)

internal class RetrofitProvider {
    private val logging = HttpLoggingInterceptor().also {
        it.setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build();

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.eu.onfido.com/v3.6/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val service: OnfidoService by lazy { retrofit.create(OnfidoService::class.java) }
}
