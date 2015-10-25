# Módulo: agents

SDK para integrar a linguagem de programação com o barramento ErlangMS. A idéia dos agentes é permitir a implementação dos serviços (Web Services) independente da linguagem de programação ou plataforma, visando integração de sistemas.

Atualmente o SDK está implementado somente na linguagem Java.

####Exemplo de Web Service ErlangMS

A classe Java a seguir, implementa um Web Service na plataforma ErlangMS. Qualquer classe Java que herde da classe base EmsServiceFacade é um Web Service. Estas classes são vistas pelo barramento como agents.



```java

@Singleton
@Startup
public class ValorAlimentacaoService extends EmsServiceFacade {

	@EJB
	private ValorAlimentacaoNegocio negocio;
	
	public ValorAlimentacao findById(IEmsRequest request){
		Integer id = request.getParamAsInt("id");
		return negocio.findById(id);
	}
	
	public List<ValorAlimentacao> find(IEmsRequest request){
		String filtro = request.getQuery("filtro");
		String fields = request.getQuery("fields");
		int limit_ini = request.getQueryAsInt("limit_ini");
		int limit_fim = request.getQueryAsInt("limit_fim");
		String sort = request.getQuery("sort");
		return negocio.pesquisar(filtro, fields, limit_ini, limit_fim, sort);
	}

	public ValorAlimentacao insert(IEmsRequest request){
		final ValorAlimentacao obj = (ValorAlimentacao) request.getObject(ValorAlimentacao.class);
		return negocio.insert(obj);
	}
	
	public ValorAlimentacao update(IEmsRequest request){
		final int id = request.getParamAsInt("id");
		ValorAlimentacao obj = negocio.findById(id);
		request.mergeObjectFromPayload(obj);
		return negocio.update(obj);
	}

}

```

####Registro no catálogo de serviço ErlangMS

É preciso fazer o registro das operações que serão expostas como serviço no catálogo de 
serviço do barramento ErlangMS. O catálogo de serviços está localizado na pasta priv/conf/catalogo e segue um layout JSON.

O exemplo a seguir demonstra o contrato dos serviços para a classe java ValorAlimentacaoService:


```json

{
    "name": "/sae/valoralimentacao",
	"comment": "Pesquisar valor alimentação",
	"owner": "sae",
	"version": "1",
	"service" : "br.unb.service.sae.ValorAlimentacaoService:find",
	"url": "/sae/valoralimentacao",
	"host": "negocio1",
	"type": "GET",
	"APIkey":"true",
	"querystring": [
		{
			"name": "filtro",
			"type": "string",
			"default" : "",
			"comment": "Filtro principal da pesquisa"
		},
		{
			"name": "fields",
			"type": "string",
			"default" : "",
			"comment": "Campos que devem ser retornados na pesquisa"
		},
		{
			"name": "limit_ini",
			"type": "int",
			"default" : "0",
			"comment": "Limite inicial do paginador"
		},
		{
			"name": "limit_fim",
			"type": "int",
			"default" : "100",
			"comment": "Limite final do paginador"
		},
		{
			"name": "sort",
			"type": "string",
			"default" : "",
			"comment": "Campos que devem ser ordenados"
		},
	],
},

{
    "name": "/sae/valoralimentacao/:id",
	"comment": "Retorna valor alimentação específico",
	"owner": "sae",
	"version": "1",
	"service" : "br.unb.service.sae.ValorAlimentacaoService:findById",
	"url": "/sae/valoralimentacao/:id",
	"host": "negocio1",
	"type": "GET",
	"APIkey":"true",
},

{
    "name": "/sae/valoralimentacao/:id",
	"comment": "Modifica valor alimentação",
	"owner": "sae",
	"version": "1",
	"service" : "br.unb.service.sae.ValorAlimentacaoService:update",
	"url": "/sae/valoralimentacao/:id",
	"host": "negocio1",
	"type": "PUT",
	"APIkey":"true",
},

{
    "name": "/sae/valoralimentacao",
	"comment": "Cadastrar valor alimentação",
	"owner": "sae",
	"version": "1",
	"service" : "br.unb.service.sae.ValorAlimentacaoService:insert",
	"url": "/sae/valoralimentacao",
	"host": "negocio1",
	"type": "POST",
	"APIkey":"true",
},

{
    "name": "/sae/valoralimentacao/:id",
	"comment": "Excluir valor alimentação",
	"owner": "sae",
	"version": "1",
	"service" : "br.unb.service.sae.ValorAlimentacaoService:delete",
	"url": "/sae/valoralimentacao",
	"host": "negocio1",
	"type": "DELETE",
	"APIkey":"true",
}


```

####Invocando os serviços no barramento ErlangMS

1. Cadastrar um novo registro para valor alimentação por meio de uma requisição POST:
```sh
curl -X POST localhost:2301/sae/valoralimentacao -d"{\"campus\":1,\"pagaBeneficio\":\"true\",\"valorBeneficio\":\"500\",\"inicioVigencia\":\"30/12/2015\"}"

{"id":5,"campus":1,"inicioVigencia":"30/12/2015","pagaBeneficio":true,"valorBeneficio":"500.00"}

```
