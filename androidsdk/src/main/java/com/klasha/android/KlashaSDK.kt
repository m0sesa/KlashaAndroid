package com.klasha.android

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.klasha.android.model.*
import com.klasha.android.model.request.*
import com.klasha.android.model.response.*
import com.klasha.android.ui.*
import com.klasha.android.ui.Login
import com.klasha.android.ui.OtpActivity
import com.klasha.android.ui.PinActivity
import com.klasha.android.ui.TransactionCredentials
import retrofit2.Response
import java.lang.ref.WeakReference
import kotlin.concurrent.thread
import kotlin.math.log


object KlashaSDK {

    private var isInitialized = false

    private var authToken: String? = null
    private var weakReferenceActivity: WeakReference<Activity> = WeakReference(null)
    private var country: Country? = null
    private var sourceCurrency: Currency? = null

    private var instance: Klasha? = null

    fun initialize(
        weakReferenceActivity: WeakReference<Activity>,
        authToken: String,
        country: Country,
        sourceCurrency: Currency
    ) {
        this.authToken = authToken
        this.weakReferenceActivity = weakReferenceActivity
        this.sourceCurrency = sourceCurrency
        this.country = country

        isInitialized = true

        instance = Klasha(authToken, weakReferenceActivity)
    }


    fun chargeCard(charge: Charge, transactionCallback: TransactionCallback) {
        /*
        Flow for Card Payment
        1. Get Exchange
        2. Use exchange to send card payment
        3. Charge card
        4. Validate payment
         */

        if (!isInitialized) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "SDK Not Initialized"
            )
            return
        }

        if (!validateAmount(charge.amount)) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "Error, Amount can not be zero"
            )
            return
        }

        transactionCallback.transactionInitiated(charge.transactionReference)

        instance?.getExchange(ExchangeRequest(this.sourceCurrency!!, country!!.currency),
            object : Klasha.ExchangeCallback {
                override fun success(exchangeResponse: Response<ExchangeResponse>) {
                    if (exchangeResponse.isSuccessful) {
                        val amount = charge.amount
                        val rate = exchangeResponse.body()!!.rate
                        val sourceAmount = amount / rate
                        val sendCardRequest = SendCardPaymentRequest(
                            charge.card!!.number, charge.card.cvv.toString(),
                            charge.card.expiryMonth.toString(), charge.card.expiryYear.toString(),
                            country!!.currency, country!!.countryCode, amount, rate,
                            sourceCurrency!!, sourceAmount,
                            charge.transactionReference, charge.phone,
                            charge.email, charge.fullName
                        )
                        sendCardPayment(sendCardRequest, transactionCallback)
                    } else {
                        transactionCallback.error(
                            weakReferenceActivity.get()!!,
                            "Something went wrong, Please make sure that the source and destination currencies are supported"
                        )
                    }
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)
                }

            })
    }

    fun bankTransfer(charge: Charge, transactionCallback: BankTransferTransactionCallback) {
        /*
        Flow for Bank Transfer
        1. Send Transaction details
         */

        if (!isInitialized) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "SDK Not Initialized"
            )
            return
        }

        if (!validateAmount(charge.amount)) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "Error, Amount can not be zero"
            )
            return
        }

        transactionCallback.transactionInitiated(charge.transactionReference)

        val bankTransferRequest = BankTransferRequest(
            charge.transactionReference,
            charge.amount,
            country!!.currency,
            charge.email,
            charge.phone
        )
        instance?.bankTransfer(
            bankTransferRequest,
            country!!.currency,
            object : Klasha.BankTransferCallback {
                override fun success(bankTransferResponse: Response<BankTransferResponse>) {
                    if (bankTransferResponse.isSuccessful) {
                        if (bankTransferResponse.body()?.status?.trim() == "error") {
                            transactionCallback.error(
                                weakReferenceActivity.get()!!,
                                bankTransferResponse.body()!!.message
                            )
                            return
                        } else {
                            val bt = bankTransferResponse.body()!!
                            val btr = BankTransferResp(
                                bt.txRef,
                                bt.meta.authorization.transferAccount,
                                bt.meta.authorization.transferBank,
                                bt.meta.authorization.accountExpiration,
                                bt.meta.authorization.transferMode,
                                bt.meta.authorization.transferAmount,
                                bt.meta.authorization.mode
                            )
                            transactionCallback.success(weakReferenceActivity.get()!!, btr)
                        }
                    }
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)
                }

            })
    }

    fun mobileMoney(charge: Charge, transactionCallback: TransactionCallback){
        /*
        Flow for mobile money
        1. Send Transaction details
         */

        if (!isInitialized) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "SDK Not Initialized"
            )
            return
        }

        val mobileMoneyRequest = MobileMoneyRequest(
            country!!.currency, charge.amount, charge.phone,
            charge.email, charge.fullName, charge.transactionReference,
            charge.mobileMoney!!.voucher, charge.mobileMoney.network

        )
        instance?.mobileMoney(mobileMoneyRequest, country!!.currency, object : Klasha.MobileMoneyCallback{
            override fun success(mobileMoneyResponse: Response<MobileMoneyResponse>) {
                if (mobileMoneyResponse.body()?.status?.trim() == "error") {
                    transactionCallback.error(
                        weakReferenceActivity.get()!!,
                        mobileMoneyResponse.body()!!.message
                    )
                    return
                } else {
                    transactionCallback.success(weakReferenceActivity.get()!!, mobileMoneyResponse.body()!!.txRef)
                }
            }

            override fun error(message: String) {
                transactionCallback.error(weakReferenceActivity.get()!!, message)
            }

        })
    }

    fun mpesa(charge: Charge, transactionCallback: TransactionCallback){
        /*
        Flow for mpesa
        1. Send Transaction details
         */

        if (!isInitialized) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "SDK Not Initialized"
            )
            return
        }

        val mpesaRequest = MPESARequest(
            charge.amount, charge.phone, charge.email,
            charge.fullName, charge.transactionReference, MPESAOption.mpesa
        )
        instance?.mpesa(mpesaRequest, country!!.currency, object : Klasha.MPESACallback{
            override fun success(mpesaResponse: Response<MPESAResponse>) {
                if (mpesaResponse.body()?.status?.trim() == "error") {
                    transactionCallback.error(
                        weakReferenceActivity.get()!!,
                        mpesaResponse.body()!!.message
                    )
                    return
                } else {
                    transactionCallback.success(weakReferenceActivity.get()!!, mpesaResponse.body()!!.txRef)
                }
            }

            override fun error(message: String) {
                transactionCallback.error(weakReferenceActivity.get()!!, message)
            }

        })
    }

    fun wallet(charge: Charge, transactionCallback: TransactionCallback){
        /*
        Flow for wallet
        1. Get Exchange
        2. Login with credentials
        3. Charge from wallet
        */

        if (!isInitialized) {
            transactionCallback.error(
                KlashaSDK.weakReferenceActivity.get()!!,
                "SDK Not Initialized"
            )
            return
        }

        instance?.getExchange(ExchangeRequest(this.sourceCurrency!!, country!!.currency),
            object : Klasha.ExchangeCallback {
                override fun success(exchangeResponse: Response<ExchangeResponse>) {
                    if (exchangeResponse.isSuccessful) {
                        val rate = exchangeResponse.body()!!.rate

                        getWalletCredentials { login ->
                            if (login.username.isEmpty() || login.password.isEmpty()){
                                Log.d("love", login.toString())
                                transactionCallback.error(weakReferenceActivity.get()!!, "Invalid login")
                                return@getWalletCredentials
                            }
                            val walletLoginRequest = WalletLoginRequest(
                                login.username, login.password
                            )
                            walletLogin(walletLoginRequest, charge, rate, transactionCallback)
                        }
                    } else {
                        transactionCallback.error(
                            weakReferenceActivity.get()!!,
                            "Something went wrong, Please make sure that the source and destination currencies are supported"
                        )
                    }
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)
                }

            })
    }


    private fun sendCardPayment(
        sendCardRequest: SendCardPaymentRequest,
        transactionCallback: TransactionCallback
    ) {
        instance?.sendCardPayment(sendCardRequest, country!!.currency, object :
            Klasha.SendCardPaymentCallback {
            override fun success(sendCardPaymentResponse: Response<SendCardPaymentResponse>) {
                if (sendCardPaymentResponse.isSuccessful) {
                    if (sendCardPaymentResponse.body()?.status?.trim() == "error") {
                        transactionCallback.error(
                            weakReferenceActivity.get()!!,
                            sendCardPaymentResponse.body()!!.message
                        )
                        return
                    }
                    getPin { pin ->
                        if (pin.isEmpty() || pin.length < 4) {
                            transactionCallback.error(weakReferenceActivity.get()!!, "Invalid pin")
                            return@getPin
                        } else {
                            val chargeCardRequest = ChargeCardRequest(
                                sendCardPaymentResponse.body()!!.data.meta.authorization.mode,
                                pin, sendCardPaymentResponse.body()!!.txRef
                            )
                            chargeCard(
                                chargeCardRequest,
                                transactionCallback
                            )
                        }
                    }
                }
            }

            override fun error(message: String) {
                transactionCallback.error(weakReferenceActivity.get()!!, message)

            }

        })
    }

    private fun chargeCard(
        chargeCardRequest: ChargeCardRequest,
        transactionCallback: TransactionCallback
    ) {
        instance?.chargeCard(chargeCardRequest, country!!.currency, object :
            Klasha.ChargeCardCallback {
            override fun success(chargeCardResponse: Response<ChargeCardResponse>) {
                if (chargeCardResponse.isSuccessful) {
                    if (chargeCardResponse.body()!!.status.trim() == "error") {
                        transactionCallback.error(
                            weakReferenceActivity.get()!!,
                            chargeCardResponse.body()!!.message
                        )
                        return
                    }
                    getOtp(chargeCardResponse.body()!!.message) { otp ->
                        if (otp.isEmpty() || otp.length < 4) {
                            transactionCallback.error(weakReferenceActivity.get()!!, "Invalid otp")
                            return@getOtp
                        } else {
                            val validatePaymentRequest = ValidatePaymentRequest(
                                otp,
                                chargeCardResponse.body()!!.flwRef,
                                PaymentType.card
                            )
                            validatePayment(
                                validatePaymentRequest,
                                transactionCallback
                            )
                        }
                    }
                }
            }

            override fun error(message: String) {
                transactionCallback.error(weakReferenceActivity.get()!!, message)

            }

        })
    }

    private fun validatePayment(
        validatePaymentRequest: ValidatePaymentRequest,
        transactionCallback: TransactionCallback
    ) {
        instance?.validatePayment(
            validatePaymentRequest,
            country!!.currency,
            object :
                Klasha.ValidatePaymentCallback {
                override fun success(validatePaymentResponse: Response<ValidatePaymentResponse>) {

                    transactionCallback.success(
                        weakReferenceActivity.get()!!,
                        validatePaymentResponse.body()!!.txRef
                    )
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)

                }
            })
    }

    private fun walletLogin(
        walletLoginRequest: WalletLoginRequest,
        charge: Charge, rate: Double,
        transactionCallback: TransactionCallback
    ) {
        instance?.walletLogin(
            walletLoginRequest,
            object :
                Klasha.WalletLoginCallback {
                override fun success(walletLoginResponse: Response<WalletLoginResponse>) {
                    if (walletLoginResponse.isSuccessful){
                        val amount = charge.amount
                        val sourceAmount = amount / rate
                        val walletPaymentRequest = MakeWalletPaymentRequest(
                            country!!.currency, charge.amount,
                            rate, sourceCurrency!!, sourceAmount,
                            charge.phone, charge.fullName,
                            charge.transactionReference, walletLoginResponse.body()!!.email
                        )
                        walletPayment(walletPaymentRequest, transactionCallback)
                    }else{
                        transactionCallback.error(weakReferenceActivity.get()!!,
                            walletLoginResponse.body()!!.error)
                    }
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)

                }
            })
    }

    private fun walletPayment(
        walletPaymentRequest: MakeWalletPaymentRequest,
        transactionCallback: TransactionCallback
    ) {
        instance?.walletPayment(
            walletPaymentRequest,
            object :
                Klasha.WalletPaymentCallback {
                override fun success(walletPaymentResponse: Response<MakeWalletPaymentResponse>) {
                    if (walletPaymentResponse.isSuccessful){
                        transactionCallback.success(
                            weakReferenceActivity.get()!!,
                            walletPaymentResponse.body()!!.walletTnxId
                        )
                    }else{
                        transactionCallback.error(weakReferenceActivity.get()!!, walletPaymentResponse.body()!!.message)
                    }
                }

                override fun error(message: String) {
                    transactionCallback.error(weakReferenceActivity.get()!!, message)

                }
            })
    }


    private fun validateAmount(amount: Double) = amount != 0.0

    private fun getPin(callback: (String) -> Unit) {
        val intent = Intent(weakReferenceActivity.get(), PinActivity::class.java)
        weakReferenceActivity.get()!!.startActivity(intent)

        thread {
            val transactionCredentials = TransactionCredentials.getInstance()

            synchronized(transactionCredentials) {
                transactionCredentials.wait()
            }

            callback(transactionCredentials.pin)
        }
    }

    private fun getOtp(message: String, callback: (String) -> Unit) {
        val intent = Intent(weakReferenceActivity.get(), OtpActivity::class.java)
        intent.putExtra("message", message)
        weakReferenceActivity.get()!!.startActivity(intent)

        thread {
            val transactionCredentials = TransactionCredentials.getInstance()

            synchronized(transactionCredentials) {
                transactionCredentials.wait()
            }

            callback(transactionCredentials.otp)
        }
    }

    private fun getWalletCredentials(callback: (Login) -> Unit) {
        val intent = Intent(weakReferenceActivity.get(), WalletLoginActivity::class.java)
        weakReferenceActivity.get()!!.startActivity(intent)

        thread {
            val transactionCredentials = TransactionCredentials.getInstance()

            synchronized(transactionCredentials) {
                transactionCredentials.wait()
            }

            callback(transactionCredentials.login)
        }
    }

    interface TransactionCallback {
        fun transactionInitiated(transactionReference: String)
        fun success(ctx: Activity, transactionReference: String)
        fun error(ctx: Activity, message: String)
    }

    interface BankTransferTransactionCallback {
        fun transactionInitiated(transactionReference: String)
        fun success(ctx: Activity, bankTransferResponse: BankTransferResp)
        fun error(ctx: Activity, message: String)
    }

}
