package com.example.sougna.data.di

import android.content.Context
import androidx.room.Room
import com.example.sougna.data.local.AppDatabase
import com.example.sougna.data.local.ProductDao
import com.example.sougna.data.repository.CategoryRepository
import com.example.sougna.data.repository.CategoryRepositoryImp
import com.example.sougna.data.repository.ProductRepository
import com.example.sougna.data.repository.ProductRepositoryImp
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton




@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // ✅ توفير قاعدة البيانات Room
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sougna_database"
        ).build()
    }


    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }


    // ✅ توفير ProductDao
    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }




    // ✅ توفير ProductRepository باستخدام المصدرين
    @Provides
    @Singleton
    fun provideProductRepository( productDao: ProductDao): ProductRepository {
        return ProductRepositoryImp(productDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(): CategoryRepository {
        return CategoryRepositoryImp()
    }

}
