import com.beforeyoudie.common.CommonTest
import com.beforeyoudie.common.di.CommonBydKotlinInjectAppComponent
import io.mockk.mockk

class AndroidTest : CommonTest() {
  override fun makeTestComponent() =
    CommonBydKotlinInjectAppComponent::class.create(
      AndroidComponent::class.create(
        mockk(context),
        ""
      )
    )

}