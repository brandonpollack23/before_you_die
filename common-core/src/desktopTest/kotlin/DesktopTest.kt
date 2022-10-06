import com.beforeyoudie.common.CommonTest
import com.beforeyoudie.common.TestBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.BydPlatformComponent

class DesktopTest : CommonTest() {
  override fun makeTestComponent(): TestBydKotlinInjectAppComponent =
    TestBydKotlinInjectAppComponent::class.create(
      BydPlatformComponent::class.java.create("")
    )
}