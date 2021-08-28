package com.klasha.android

import android.app.Activity
import com.klasha.android.model.Currency
import com.klasha.android.model.request.*
import com.klasha.android.model.response.*
import com.klasha.android.service.ApiFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.ref.WeakReference
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException

internal class Klasha(
    private val authToken: String,
    private val weakReferenceActivity: WeakReference<Activity>
) {

    fun getExchange(exchangeRequest: ExchangeRequest, exchangeCallBack: ExchangeCallback) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .exchangeMoney(exchangeRequest)
            .enqueue(object : Callback<ExchangeResponse> {
                override fun onResponse(
                    call: Call<ExchangeResponse>,
                    response: Response<ExchangeResponse>
                ) {
                    exchangeCallBack.success(response)
                }

                override fun onFailure(call: Call<ExchangeResponse>, t: Throwable) {
                    val message = parseError(t)
                    exchangeCallBack.error(message)
                }
            })
    }

    fun validatePayment(
        validatePaymentRequest: ValidatePaymentRequest,
        destinationCurrency: Currency,
        validatePaymentCallback: ValidatePaymentCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .validatePayment(validatePaymentRequest, destinationCurrency)
            .enqueue(object : Callback<ValidatePaymentResponse> {
                override fun onResponse(
                    call: Call<ValidatePaymentResponse>,
                    response: Response<ValidatePaymentResponse>
                ) {
                    validatePaymentCallback.success(response)
                }

                override fun onFailure(call: Call<ValidatePaymentResponse>, t: Throwable) {
                    val message = parseError(t)
                    validatePaymentCallback.error(message)
                }

            })
    }

    fun sendCardPayment(
        sendCardPaymentRequest: SendCardPaymentRequest,
        destinationCurrency: Currency,
        sendCardPaymentCallback: SendCardPaymentCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .sendCardPayment(sendCardPaymentRequest, destinationCurrency)
            .enqueue(object : Callback<SendCardPaymentResponse> {
                override fun onResponse(
                    call: Call<SendCardPaymentResponse>,
                    response: Response<SendCardPaymentResponse>
                ) {
                    sendCardPaymentCallback.success(response)
                }

                override fun onFailure(call: Call<SendCardPaymentResponse>, t: Throwable) {
                    val message = parseError(t)
                    sendCardPaymentCallback.error(message)
                }

            })
    }

    fun chargeCard(
        chargeCardRequest: ChargeCardRequest,
        destinationCurrency: Currency,
        chargeCardCallback: ChargeCardCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .chargeCard(chargeCardRequest, destinationCurrency)
            .enqueue(object : Callback<ChargeCardResponse> {
                override fun onResponse(
                    call: Call<ChargeCardResponse>,
                    response: Response<ChargeCardResponse>
                ) {
                    chargeCardCallback.success(response)
                }

                override fun onFailure(call: Call<ChargeCardResponse>, t: Throwable) {
                    val message = parseError(t)
                    chargeCardCallback.error(message)
                }
            })
    }

    fun bankTransfer(
        bankTransferRequest: BankTransferRequest,
        destinationCurrency: Currency,
        bankTransferCallback: BankTransferCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .bankTransfer(bankTransferRequest, destinationCurrency)
            .enqueue(object : Callback<BankTransferResponse> {
                override fun onResponse(
                    call: Call<BankTransferResponse>,
                    response: Response<BankTransferResponse>
                ) {
                    bankTransferCallback.success(response)
                }

                override fun onFailure(call: Call<BankTransferResponse>, t: Throwable) {
                    val message = parseError(t)
                    bankTransferCallback.error(message)
                }

            })
    }

    fun mobileMoney(
        mobileMoneyRequest: MobileMoneyRequest,
        destinationCurrency: Currency,
        mobileMoneyCallback: MobileMoneyCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .mobileMoney(mobileMoneyRequest, destinationCurrency)
            .enqueue(object : Callback<MobileMoneyResponse> {
                override fun onResponse(
                    call: Call<MobileMoneyResponse>,
                    response: Response<MobileMoneyResponse>
                ) {
                    mobileMoneyCallback.success(response)
                }

                override fun onFailure(call: Call<MobileMoneyResponse>, t: Throwable) {
                    val message = parseError(t)
                    mobileMoneyCallback.error(message)
                }

            })
    }

    fun mpesa(
        mpesaRequest: MPESARequest,
        destinationCurrency: Currency,
        mpesaCallback: MPESACallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .mpesa(mpesaRequest, destinationCurrency)
            .enqueue(object : Callback<MPESAResponse> {
                override fun onResponse(
                    call: Call<MPESAResponse>,
                    response: Response<MPESAResponse>
                ) {
                    mpesaCallback.success(response)
                }

                override fun onFailure(call: Call<MPESAResponse>, t: Throwable) {
                    val message = parseError(t)
                    mpesaCallback.error(message)
                }

            })
    }

    fun walletLogin(
        walletLoginRequest: WalletLoginRequest,
        walletLoginCallback: WalletLoginCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .walletLogin(walletLoginRequest)
            .enqueue(object : Callback<WalletLoginResponse> {
                override fun onResponse(
                    call: Call<WalletLoginResponse>,
                    response: Response<WalletLoginResponse>
                ) {
                    walletLoginCallback.success(response)
                }

                override fun onFailure(call: Call<WalletLoginResponse>, t: Throwable) {
                    val message = parseError(t)
                    walletLoginCallback.error(message)
                }

            })
    }

    fun walletPayment(
        walletPaymentRequest: MakeWalletPaymentRequest,
        walletPaymentCallback: WalletPaymentCallback
    ) {
        ApiFactory.createService(weakReferenceActivity.get()!!, authToken)
            .walletPayment(walletPaymentRequest)
            .enqueue(object : Callback<MakeWalletPaymentResponse> {
                override fun onResponse(
                    call: Call<MakeWalletPaymentResponse>,
                    response: Response<MakeWalletPaymentResponse>
                ) {
                    walletPaymentCallback.success(response)
                }

                override fun onFailure(call: Call<MakeWalletPaymentResponse>, t: Throwable) {
                    val message = parseError(t)
                    walletPaymentCallback.error(message)
                }

            })
    }

    private fun parseError(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                when (t.code()) {
                    HttpsURLConnection.HTTP_UNAUTHORIZED -> "Unauthorised User"
                    HttpsURLConnection.HTTP_FORBIDDEN -> "Forbidden"
                    HttpsURLConnection.HTTP_INTERNAL_ERROR -> "Internal server error"
                    HttpsURLConnection.HTTP_BAD_REQUEST -> "Bad Request"
                    else -> t.localizedMessage
                }
            }
            is UnknownHostException -> {
                "Limited Connectivity, Please Check internet"
            }
            is SSLHandshakeException -> {
                "Limited Connectivity, Please Check internet"
            }
            is SocketException -> {
                "Limited Connectivity, Please Check internet"
            }
            else -> {
                t.message.toString()
            }
        }
    }

    interface ExchangeCallback {
        fun success(exchangeResponse: Response<ExchangeResponse>)
        fun error(message: String)
    }

    interface SendCardPaymentCallback {
        fun success(sendCardPaymentResponse: Response<SendCardPaymentResponse>)
        fun error(message: String)
    }

    interface ChargeCardCallback {
        fun success(chargeCardResponse: Response<ChargeCardResponse>)
        fun error(message: String)
    }

    interface ValidatePaymentCallback {
        fun success(validatePaymentResponse: Response<ValidatePaymentResponse>)
        fun error(message: String)
    }

    interface BankTransferCallback {
        fun success(bankTransferResponse: Response<BankTransferResponse>)
        fun error(message: String)
    }

    interface MobileMoneyCallback {
        fun success(mobileMoneyResponse: Response<MobileMoneyResponse>)
        fun error(message: String)
    }

    interface MPESACallback {
        fun success(mpesaResponse: Response<MPESAResponse>)
        fun error(message: String)
    }

    interface WalletLoginCallback {
        fun success(walletLoginResponse: Response<WalletLoginResponse>)
        fun error(message: String)
    }

    interface WalletPaymentCallback {
        fun success(walletPaymentResponse: Response<MakeWalletPaymentResponse>)
        fun error(message: String)
    }
}