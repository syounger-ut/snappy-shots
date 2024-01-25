import com.authService.repositories.UserRepository
import com.authService.repositories.PhotoRepository
import com.google.inject.{AbstractModule, Provides}

import javax.inject.Singleton

// $COVERAGE-OFF$
class SnappyShotsModule extends AbstractModule {
  @Provides @Singleton
  // This is used to inject the UserRepository which needs initialization into other classes
  def providesUserRepository(): UserRepository = new UserRepository
  @Provides @Singleton
  def providesPhotoRepository(): PhotoRepository = new PhotoRepository
}

// $COVERAGE-ON$
