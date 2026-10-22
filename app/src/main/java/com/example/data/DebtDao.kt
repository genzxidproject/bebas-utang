package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isPaidOff ASC, dueDayOfMonth ASC, priorityOrder ASC")
    fun observeAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebtsOnce(): List<DebtEntity>

    @Query("SELECT COUNT(*) FROM debts")
    suspend fun getDebtCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts")
    suspend fun clearAllDebts()

    // Monthly Expenses
    @Query("SELECT * FROM monthly_expenses ORDER BY monthKey DESC, amount DESC")
    fun observeAllExpenses(): Flow<List<MonthlyExpenseEntity>>

    @Query("SELECT * FROM monthly_expenses")
    suspend fun getAllExpensesOnce(): List<MonthlyExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: MonthlyExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<MonthlyExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: MonthlyExpenseEntity)

    @Query("DELETE FROM monthly_expenses")
    suspend fun clearAllExpenses()

    // Payment History
    @Query("SELECT * FROM payment_history ORDER BY paidAtMillis DESC")
    fun observePaymentHistory(): Flow<List<PaymentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentHistory(payment: PaymentHistoryEntity)

    @Query("DELETE FROM payment_history")
    suspend fun clearAllPaymentHistory()
}
