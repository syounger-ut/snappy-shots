import com.snappyShots.repositories.{
  PhotoRepository,
  StorageAdapter,
  StorageRepository,
  UserRepository
}
import com.google.inject.{AbstractModule, Provides}

import javax.inject.Singleton

// $COVERAGE-OFF$
class SnappyShotsModule extends AbstractModule {
  @Provides @Singleton
  // This is used to inject the UserRepository which needs initialization into other classes
  def providesUserRepository(): UserRepository = new UserRepository
  @Provides @Singleton
  def providesPhotoRepository(): PhotoRepository = new PhotoRepository

  @Provides @Singleton
  def providesStorageRepository(): StorageRepository = new StorageRepository(
    new StorageAdapter
  )
}

// $COVERAGE-ON$
