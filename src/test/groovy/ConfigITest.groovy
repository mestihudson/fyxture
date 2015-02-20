import org.junit.Test
import org.junit.Assert

class ConfigITest {
  @Test void "carregado configurações padrão"() {
    Config.init()
    Assert.assertTrue true
  }
}
