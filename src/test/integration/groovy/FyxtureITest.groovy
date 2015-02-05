import org.junit.Test
import org.junit.Ignore

import org.yaml.snakeyaml.Yaml

class FyxtureITest {
  @Test @Ignore void "test integração"() {
    Yaml yaml = new Yaml()
    String document = """
common:
  datasource: h2

"""
    new File("config.yml").write(document)
    //println yaml.dump(yaml.load(document))
    // Fyxture.clear()
  }
}
