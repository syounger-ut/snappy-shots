import com.authService.repositories.UserRepository
import com.google.inject.{AbstractModule, Provides}
import slick.jdbc.PostgresProfile

import javax.inject.Singleton

class SnappyShotsModule extends AbstractModule {
  @Provides @Singleton
  // This is used to inject the UserRepository which needs initialization into other classes
  def providesUserRepository(): UserRepository = new UserRepository
}
