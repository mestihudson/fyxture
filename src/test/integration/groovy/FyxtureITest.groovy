import org.junit.Test
import org.junit.After
import org.junit.Before
import org.junit.Ignore

import org.yaml.snakeyaml.Yaml

class FyxtureITest {
  def file
  def yaml = new Yaml()

  @Before void "criar arquivo de configuração"() {
    file = new File("config.yml")
  }

  @After void "eliminar arquivo de configuração"() {
    file.delete()
  }

  void config(String content) {
    file.write content
  }

  @Test void "test integração"() {
    config("""
common:
  datasource: h2
""")
  }
}
