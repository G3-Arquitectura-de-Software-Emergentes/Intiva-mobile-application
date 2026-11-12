package com.resolum.intiva.features.finances.presentation.transactions

import androidx.lifecycle.viewModelScope
import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.core.common.viewmodel.BaseViewModel
import com.resolum.intiva.core.network.model.NetworkResult
import com.resolum.intiva.core.ui.snackbar.SnackBarBus
import com.resolum.intiva.core.ui.snackbar.SnackBarType
import com.resolum.intiva.features.finances.domain.models.RegisterTransactionRequest
import com.resolum.intiva.features.finances.domain.models.TransactionType
import com.resolum.intiva.features.finances.domain.usecase.GetTransactionsByOwnerIdUseCase
import com.resolum.intiva.features.finances.domain.usecase.GetTransactionByIdUseCase
import com.resolum.intiva.features.finances.domain.usecase.ObserveTransactionSyncStatusUseCase
import com.resolum.intiva.features.finances.domain.usecase.RegisterIndividualTransactionUseCase
import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.Category
import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.FinancialAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the state and logic related to financial transactions.
 *
 * This ViewModel handles the registration of individual transactions and updates the UI state accordingly.
 *
 * @property registerIndividualTransactionUseCase The use case for registering an individual transaction.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val registerIndividualTransactionUseCase: RegisterIndividualTransactionUseCase,
    private val getTransactionsByOwnerIdUseCase: GetTransactionsByOwnerIdUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val observeTransactionSyncStatusUseCase: ObserveTransactionSyncStatusUseCase,
) : BaseViewModel() {

    /** Backing property for the UI state, initialized with a default TransactionUiState. */
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTransactionSyncStatusUseCase().collect { summary ->
                _uiState.update { it.copy(syncStatusSummary = summary) }
            }
        }
    }

    /**
     * Registers a financial transaction based on the provided parameters.
     *
     * This function constructs a [RegisterTransactionRequest] using the input parameters and invokes the
     * [registerIndividualTransactionUseCase] to perform the registration. The UI state is updated based on
     * the result of the operation, reflecting loading, success, or error states.
     *
     * @param amount The amount of the transaction as a string.
     * @param category The optional category associated with the transaction.
     * @param account The optional financial account associated with the transaction.
     * @param transactionType The type of the transaction (e.g., income, expense).
     */
    fun registerIndividualTransaction(
        amount: String,
        category: Category?,
        account: FinancialAccount?,
        transactionType: TransactionType,
        ownerType: String,
        description: String = ""
    ) {
        if (_uiState.value.state is UiState.Loading) return

        if (category == null) {
            viewModelScope.launch {
                SnackBarBus.send(
                    "Transacción sin categoría asignada",
                    SnackBarType.Error
                )
            }
            return
        }

        if (amount.endsWith(".")) {
            viewModelScope.launch {
                SnackBarBus.send(
                    "Ingresa los centavos o elimina el punto decimal",
                    SnackBarType.Error
                )
            }
            return
        }

        if (amount == "0.00" || amount == "0") {
            viewModelScope.launch {
                SnackBarBus.send(
                    "El monto debe ser mayor a cero",
                    SnackBarType.Error
                )
            }
            return
        }

        if (account == null) {
            viewModelScope.launch {
                SnackBarBus.send(
                    "Transacción sin cuenta financiera asignada",
                    SnackBarType.Error
                )
            }
            return
        }

        val amountBigDecimal = runCatching {
            amount.toBigDecimal()
        }.getOrElse {
            viewModelScope.launch {
                SnackBarBus.send(
                    "Monto inválido: ${it.message}",
                    SnackBarType.Error
                )
            }
            return
        }

        val request = RegisterTransactionRequest(
            amount = amountBigDecimal,
            currencyCode = "PEN",
            description = description.trim().ifEmpty { category.name },
            financialAccountId = account.id,
            transactionType = transactionType,
            categoryId = category.id,
            ownerType = ownerType
        )

        _uiState.update {
            it.copy(state = UiState.Loading)
        }

        safeLaunch {

            when (val result = registerIndividualTransactionUseCase(request)) {

                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            state = UiState.Success(result.data),
                            navigateBack = true
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            state = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }

                    viewModelScope.launch {
                        SnackBarBus.send(
                            "Error al registrar la transacción: ${result.message}",
                            SnackBarType.Error
                        )
                    }
                }
            }
        }
    }

    fun getTransactionsByOwnerId(
        transactionType: TransactionType? = null,
        onlyLatest: Boolean = false
    ) {

        safeLaunch {

            _uiState.update {
                it.copy(
                    transactionsState = UiState.Loading
                )
            }

            when (
                val result = getTransactionsByOwnerIdUseCase(
                    transactionType = transactionType?.name,
                    onlyLatest = onlyLatest
                )
            ) {

                is NetworkResult.Success -> {

                    _uiState.update {
                        it.copy(
                            transactionsState = UiState.Success(result.data)
                        )
                    }
                }

                is NetworkResult.Error -> {

                    _uiState.update {
                        it.copy(
                            transactionsState = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }

                    viewModelScope.launch {
                        SnackBarBus.send(
                            "Error al obtener transacciones: ${result.message}",
                            SnackBarType.Error
                        )
                    }
                }
            }
        }
    }

    fun getTransactionById(id: Long) {
        safeLaunch {
            _uiState.update {
                it.copy(transactionDetailState = UiState.Loading)
            }

            when (val result = getTransactionByIdUseCase(id)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(transactionDetailState = UiState.Success(result.data))
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            transactionDetailState = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }
                }
            }
        }
    }

}
