package app.todate.onfido

import com.onfido.workflow.WorkflowConfig
import timber.log.Timber

class OnfidoHandler {
    private val service = RetrofitProvider().service
    var config: WorkflowConfig? = null

    suspend fun init(
        firstName: String,
        lastName: String,
        workflowId: String,
        authHeader: String,
    ) {
        val createApplicant = service.createApplicant(
            authHeader = authHeader,
            req = ApplicantReq(
                firstName = firstName,
                lastName = lastName,
            )
        )

        Timber.d("create applicant id: ${createApplicant.id}")

        val applicant = service.retrieveApplicant(
            authHeader = authHeader,
            applicantId = createApplicant.id,
        )

        Timber.d("retrieve applicant id: ${applicant.id}")
        Timber.d("c&r applicant id match: ${applicant.id == createApplicant.id}")

        val token = service.createSdkToken(
            authHeader = authHeader,
            req = TokenReq(
                applicantId = applicant.id,
                appId = BuildConfig.APPLICATION_ID,
            )
        )

        Timber.d("sdk token: ${token.token}")

        val wfResp = service.workflowRun(
            authHeader = authHeader,
            req = WfReq(
                wfId = workflowId,
                applicantId = applicant.id,
            )
        )

        Timber.d("workflow run id: ${wfResp.id}")

        config = WorkflowConfig.Builder(
            workflowRunId = wfResp.id,
            sdkToken = token.token
        ).build()

        Timber.d("sdk initiated")
    }
}
