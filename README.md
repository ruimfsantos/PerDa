# PerDa2Disco - Personnal Data to Discovery (DataDefender/RedDataSense fork)

Tabela de conteúdos
-------------------
- [Aviso](#Aviso)
- [Principais Características](#Principais Características)
- [Pré-requisitos](#Pré-requisitos)
- [Construa a partir da fonte](#Construa a partir da fonte)
- [Contribua](#Contribua)
- [Como Executar](#Como Executar)


Aviso
-----
Este projecto nasceu no âmbito de um tese de dissertação de mestrado em informática e de computadores.
E é uma derivação (fork) do trabalho original DataDefender (https://github.com/armenak/DataDefender) e de uma derivação chamada RedDataSense (https://github.com/redglue/redsense).
As principais razões da derivação consistiram em:
- Melhorar as técnicas usadas na descoberta de dados pessoais, potencialemente sensíveis, quer ao nível de dados estruturados e não estruturados;
- Introdução de modelos na língua portuguesa;
- Adição de procura por padrões no texto, permitindo encontrar termos compostos;
- Melhorar a eficiência de pesquisa (alargar a pesquisa em redes de computadores de uma forma remota e centralizada); 
- Melhorar os relatórios (logs), preparando os resultados obtidos para facilitar a análise de risco na governação dos dados pessoais (de acordo com o RGPD).

O código é considerado de fonte aberta sendo que está disponível. A licença é a mesma que o projeto original.

A implementação é baseada em [Apache OpenNLP](https://opennlp.apache.org/)


Principais Características
--------------------------
1. Identifica e considera dados pessoais usando o Reconhecimento Nominal de Entidades (todos fornecidos pelo OpenNLP), recorrendo a:
	a) Modelos de Maximun Entropy;
	b) Dicionários;
	c) Expressões Regulares (REGEX)
	d) Consulta especifica de padrões
2. Permite a descoberta de dados pessoais em ficheiros estruturados e não estruturados.
3. Pode ser executado em qualquer plataforma (sistemas operativos diferentes)
4. Suporta as seguintes bases de dados: Oracle, MS SQL Server, DB2, MySQL e Postgres


Pré-requisitos
--------------
1. JDK 1.8+
2. Maven 3+


Construa a partir da fonte
--------------------------
1. Faça o download do arquivo ZIP e descompacte num diretório à sua escolha, ou clone o repositório
2. mvn install
3. PerDa2Disco.jar estará localizado no destino do diretório "target"
4. Deverá ajustar os ficheiros de propriedades


Contribua
---------
Se quiser pode contribuir....

Os colaboradores são encorajados a desembolsar este repositório e a emitir pedidos pull. Se você gostaria de contribuir, por favor
1. Fork it
2. Crie sua ramificação (git checkout -b new-feature)
3. Confirme suas alterações (git commit -am 'Adicionar novo recurso')
4. Carregue para o Branch (git push origin new-feature)
5. Criar nova solicitação pull (Pull Request)


Como Executar
----------
O programa é para ser executado em linha de comando (cmd ou powershell).
Para executar, primeiro construa a aplicação (ver tópico acima - mvn install). Este irá gerar um arquivo jar executável no diretório "target".
Depois disso, você pode obter ajuda digitando:

     java -jar PerDa2Disco.jar --help

Embora não tenha sido modificado a componente de anonymizer, o modo anonymizer está disponível. Para além disso estão disponíveis três diferentes modos de descoberta:
1. Descoberta de ficheiros (não estuturados);
2. Identificação das tabelas e colunas (Estruturados);
3. Descoberta do conteúdo de Bases de dados.
Para modos de descoberta em dados estruturados, precisa fornecer
o arquivo de propriedades da base dados que define a conectividade à mesma (DB.properties).

Todos os modos suportam uma lista opcional de tabelas no final para usar para descobrir ou anonimizar uma tabela específica ou uma lista de tabelas.
