package com.resolum.intiva.core.navigation.routes

/**
 * Object containing constants for navigation routes used throughout the application. This centralizes route definitions, making it easier to manage and avoid typos.
 */
object NavRoutes {
    const val HOME                        = "home"
    const val MONTHLY_PLAN = "monthly_plan"
    const val TRANSACTIONS                = "transactions"
    const val TRANSACTION_DETAIL          = "transaction_detail/{transactionId}"
    const val SPENDING_LIMIT_ALERT        = "spending_limit_alert"
    const val SAVINGS_GOALS               = "savings_goals"
    const val SAVINGS_GOAL_CREATE         = "savings_goal_create/{accountId}"
    const val SAVINGS_GOAL_EDIT           = "savings_goal_edit/{id}"
    const val SAVINGS_GOAL_DETAIL         = "savings_goal_detail/{accountId}/{goalId}"
    const val SAVINGS_GOAL_CONTRIBUTE     = "savings_goal_contribute/{accountId}/{goalId}"
    const val SAVINGS_GOAL_COMPLETED      = "savings_goal_completed/{accountId}/{goalId}"
    const val SAVINGS_GOAL_UNCOMPLETED    = "savings_goal_uncompleted/{accountId}/{goalId}"
    const val FAMILY                      = "family"
    const val INVITE_MEMBER               = "family/invite"
    const val FAMILY_ROLES                = "family/roles"
    const val INVITATION_DETAIL           = "family/invitation"
    const val INVITATION_DEEP_LINK        = "invitation/{token}"
    const val PROFILE                     = "profile"
    const val EDIT_PROFILE                = "edit_profile"
    const val CONFIG                      = "config"
    const val PRIVACY                     = "privacy"
    const val HELP                        = "help"
    const val NOTIFICATIONS               = "notifications"
    const val IN_APP_NOTIFICATIONS        = "in_app_notifications"
    const val APPEARANCE                  = "appearance"

    const val NEW_INCOME           = "transactions/new_income"
    const val NEW_EXPENSE          = "transaction/new_expense"

    fun transactionDetail(transactionId: Long) = "transaction_detail/$transactionId"

    const val MANAGE_CATEGORIES           = "manage_categories"

    const val FINANCIAL_ACCOUNTS = "financial_accounts"
    const val CREATE_FINANCIAL_ACCOUNT = "create_financial_account"
    const val FINANCIAL_ACCOUNT_DETAIL = "financial_account_detail/{accountId}"
    const val EDIT_FINANCIAL_ACCOUNT = "edit_financial_account/{accountId}"

    fun financialAccountDetail(accountId: Long) = "financial_account_detail/$accountId"
    fun editFinancialAccount(accountId: Long) = "edit_financial_account/$accountId"

    val BOTTOM_NAV_ROUTES = setOf(HOME, TRANSACTIONS, SAVINGS_GOALS, FAMILY, PROFILE)
}
