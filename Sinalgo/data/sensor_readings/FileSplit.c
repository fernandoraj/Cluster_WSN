#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <locale.h>

int main(){
  setlocale(LC_ALL,"ptb");
  FILE *arq1,*arq2,*arqOrigem;
  char nome1[50],nome2[50],c,linhaAtual[100];
  
  char nome[50];
  
  printf("Programa para dividir o conteúdo de 1 Arquivo-Texto dado em 2 novos arquivos (NovoArquivo?.txt)\n");
  printf("Digite o nome do arquivo texto a ser dividido: ");
  gets(nome);

  arqOrigem = fopen(nome,"r");
  if(arqOrigem == NULL){
    printf("Erro na abertura do arquivo %s!\n",nome);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Abertura do arquivo %s realizada com sucesso!\n",nome);
  }

  printf("Digite o nome do 1º arquivo texto a ser gerado: ");
  gets(nome1);
  printf("Digite o nome do 2º arquivo texto a ser gerado: ");
  gets(nome2);

  printf("Os nomes dos arquivos-texto digitados foram: %s e %s\n",nome1,nome2);
  system("pause");

  arq1 = fopen(nome1,"w");
  if(arq1 == NULL){
    printf("Erro na abertura do arquivo %s!\n",nome1);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Abertura do arquivo %s realizada com sucesso!\n",nome1);
  }

  arq2 = fopen(nome2,"w");
  if(arq2 == NULL){
    printf("Erro na abertura do arquivo %s!\n",nome2);
    //system("pause");
    exit(1);
  }
  else{
  	printf("Abertura do arquivo %s realizada com sucesso!\n",nome2);
  }


  int num = 1, tot = 2;

/*
  //nome1 = nome1+num+".txt";
  sprintf(nome1, "%s%i.txt", nome1, num);
  arq1 = fopen(nome1,"w");
  while(arq1 == NULL){
  	num++;
    sprintf(nome1,"%s%i.txt", nome1, num);
    arq1 = fopen(nome1,"w+");
    //printf("Erro na criação do arquivo %s!\n",nomeNovo);
    //system("pause");
    //exit(1);
  }
  printf("Criação do arquivo %s realizada com sucesso!\n",nome1);
  
  num = 1;
  //nome2 = nome2+num+".txt";
  sprintf(nome2, "%s%i.txt", nome2, num);
  arq2 = fopen(nome2,"w");
  while(arq2 == NULL){
  	num++;
    sprintf(nome2,"%s%i.txt", nome2, num);
    arq1 = fopen(nome1,"w+");
    //printf("Erro na criação do arquivo %s!\n",nomeNovo);
    //system("pause");
    //exit(1);
  }
  printf("Criação do arquivo %s realizada com sucesso!\n",nome2);
*/
  
  
  int i,num_linhas = 1;
  while((c = fgetc(arqOrigem)) != EOF)
  {
  	//printf("%c",c);
    if(c=='\n'){
    	num_linhas++;
	}
  }
  printf("\nO número de linhas do arquivo %s é %d\n",nome,num_linhas);

  int metade_linhas = num_linhas/2;
  
  printf("\nO número da metade das linhas do arquivo %s é %d\n",nome,metade_linhas);
  
  system("pause");
  
  //Voltando ao início de "arqOrigem"
  rewind(arqOrigem);
  
  int linha_atual = 1;
  //Dividindo as linhas para o arq1
  while(linha_atual < metade_linhas)
  {
//  	printf("%c",c);
	fgets(linhaAtual, 100, arqOrigem);
//	printf("\nLinha Atual 01 = %s\n",linhaAtual);
	fputs(linhaAtual,arq1);
	linha_atual++;
  }
  //Concatenando caracteres do arq2
  while(linha_atual < num_linhas)
  {
//  	printf("%c",c);
	fgets(linhaAtual, 100, arqOrigem);
//	printf("\nLinha Atual 02 = %s\n",linhaAtual);
	fputs(linhaAtual,arq2);
	linha_atual++;
  }
  //Fechando os arquivos abertos
  fclose(arq1);
  fclose(arq2);
  
  fclose(arqOrigem);
  
  //system("pause");
  return 0;
}
