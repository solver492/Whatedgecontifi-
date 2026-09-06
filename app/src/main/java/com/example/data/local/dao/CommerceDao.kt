package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AffiliateEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PriceContactEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShippingAgencyEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommerceDao {

    // --- PRODUITS ---
    @Query("SELECT * FROM ecommerce_products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM ecommerce_products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // --- CATÉGORIES ---
    @Query("SELECT * FROM ecommerce_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // --- FOURNISSEURS ---
    @Query("SELECT * FROM ecommerce_suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    // --- CONTACTS & TARIFS ---
    @Query("SELECT * FROM ecommerce_price_contacts ORDER BY supplierName ASC")
    fun getAllPriceContacts(): Flow<List<PriceContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceContact(contact: PriceContactEntity)

    @Update
    suspend fun updatePriceContact(contact: PriceContactEntity)

    @Delete
    suspend fun deletePriceContact(contact: PriceContactEntity)

    // --- AGENCES LIVRAISON ---
    @Query("SELECT * FROM ecommerce_shipping_agencies ORDER BY name ASC")
    fun getAllShippingAgencies(): Flow<List<ShippingAgencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShippingAgency(agency: ShippingAgencyEntity)

    @Update
    suspend fun updateShippingAgency(agency: ShippingAgencyEntity)

    @Delete
    suspend fun deleteShippingAgency(agency: ShippingAgencyEntity)

    // --- AFFILIÉS ---
    @Query("SELECT * FROM ecommerce_affiliates ORDER BY fullName ASC")
    fun getAllAffiliates(): Flow<List<AffiliateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffiliate(affiliate: AffiliateEntity)

    @Update
    suspend fun updateAffiliate(affiliate: AffiliateEntity)

    @Delete
    suspend fun deleteAffiliate(affiliate: AffiliateEntity)

    // --- COMMANDES & CLIENTS À APPELER ---
    @Query("SELECT * FROM ecommerce_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM ecommerce_orders WHERE status = 'PENDING_CONFIRMATION' ORDER BY createdAt DESC")
    fun getOrdersToCall(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE ecommerce_orders SET status = :newStatus, customerCallNotes = :notes, callAttemptsCount = callAttemptsCount + 1 WHERE id = :orderId")
    suspend fun updateOrderStatusAndNotes(orderId: String, newStatus: String, notes: String)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}
