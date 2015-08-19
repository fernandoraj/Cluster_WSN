#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <locale.h>

int main(){
  setlocale(LC_ALL,"ptb");
  FILE *arq1,*arq2,*arqConcat;
  char nome1[50],nome2[50],c;
  printf("Programa para concatenar o conteúdo de 2 Arquivos-Textos dados em um novo arquivo, com nome dado pelo usuário\n");
  printf("Digite o nome do 1º arquivo texto a ser concatenado: ");
  gets(nome1);
  printf("Digite o nome do 2º arquivo texto a ser concatenado: ");
  gets(nome2);

  printf("Os nomes dos arquivos-texto digitados foram: %s e %s\n",nome1,nome2);
  system("pause");
  
  arq1 = fopen(nome1,"r");
  if(arq1 == NULL){
    printf("Erro na abertura do arquivo %s!\n",nome1);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Abertura do arquivo %s realizada com sucesso!\n",nome1);
  }
  
  arq2 = fopen(nome2,"r");
  if(arq2 == NULL){
    printf("Erro na abertura do arquivo %s!\n",nome2);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Abertura do arquivo %s realizada com sucesso!\n",nome2);
  }
  
  int num = 1;
  char nome[50];
  printf("Digite o nome do novo arquivo texto a ser gerado: ");
  gets(nome);
  arqConcat = fopen(nome,"w+");
  if(arqConcat == NULL){
    printf("Erro na abertura do arquivo: %s!\n",nome);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Criação do arquivo %s realizada com sucesso!\n",nome);
  }
  system("pause");
  
  //nome = "NovoArquivo"+num+".txt";
  /*
  sprintf(nome, "%s%i.txt","NovoArquivo",num);
  arqConcat = fopen(nome,"w+");
  while(arqConcat == NULL){
  	num++;
    sprintf(nome,"%s%i.txt","NovoArquivo",num);
    arqConcat = fopen(nome,"w+");
    //printf("Erro na criação do arquivo %s!\n",nomeNovo);
    //system("pause");
    //exit(1);
  }
  printf("Criação do arquivo %s realizada com sucesso!\n",nome);
  */
  
  //int i,num_linhas = 1;
  //Concatenando caracteres do arq1
  while((c = fgetc(arq1)) != EOF)
  {
//  	printf("%c",c);
	fputc(c,arqConcat);
  }
  //Concatenando caracteres do arq2
  while((c = fgetc(arq2)) != EOF)
  {
//  	printf("%c",c);
	fputc(c,arqConcat);
  }
  //Fechando os arquivos abertos
  fclose(arq1);
  fclose(arq2);
  
  //Voltando ao início de "arqConcat"
  //rewind(arqConcat);
  //Impressão de todo o conteúdo do arquivo
  /*
  while((c = fgetc(arqConcat)) != EOF)
  {
  	printf("%c",c);
  }
  */
//  printf("\nO número de linhas do arquivo %s é %d\n",nome,num_linhas);
  fclose(arqConcat);
  
  //system("pause");
  return 0;
}
