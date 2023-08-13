#include<stdio.h>
#include<stdlib.h>
#define MaxVertices 100 
#define MaxWeight 32767 
#define MAX 100
#define INF         (~(0x1<<31)) 
typedef struct{ 
    char Vertices[MaxVertices];  
    int Edge[MaxVertices][MaxVertices]; 
    int numV; 
    int numE; 
}AdjMatrix;

void CreateGraph(AdjMatrix *G) 
{ 
    int n,e,vi,vj,w,i,j;
    printf("请输入图的顶点数和边数（以空格分隔）：");
    scanf("%d%d",&n,&e);
    G->numV=n;G->numE=e;
    for(i=0;i<n;i++) 
        for(j=0;j<n;j++)
            { 
            if(i==j)
                G->Edge[i][j]=0;
            else 
                G->Edge[i][j]=32767;
            }
    for(i=0;i<n;i++)
        for(i=0;i<G->numV;i++) 
        { 
        printf("请输入第%d个顶点的信息(整型):",i+1);  
        scanf(" %c",&G->Vertices[i]);
        }
    printf("\n");

    for(i=0;i<G->numE;i++)
    { 
        printf("请输入边的信息i,j,w(以空格分隔):");
        scanf("%d%d%d",&vi,&vj,&w); 

        G->Edge[vi-1][vj-1]=w;//①
    }
}
void DispGraph(AdjMatrix G) 
{ 
    int i,j;
    printf("\n输出顶点的信息（整型）:\n");
    for(i=0;i<G.numV;i++)
        printf("%8c",G.Vertices[i]);

    printf("\n输出邻接矩阵:\n");
    printf("\t");
    for(i=0;i<G.numV;i++)
        printf("%8c",G.Vertices[i]);

    for(i=0;i<G.numV;i++)
    { 
        printf("\n%8d",i+1);
        for(j=0;j<G.numV;j++)
        { 
    
            printf("%8s", "∞");
        else
            printf("%8d",G.Edge[i][j]);
        }
        printf("\n");   
    }
}
void dijkstra(AdjMatrix *G)
{
    int i,j;
    int min,minid;
    int tmp;
    int vs;
    int prev[MAX] = {0};
    int dist[MAX] = {0};
    int visited[MAX];     

    printf("请输入要查询的单源顶点");
    scanf("%d",&vs);
    vs-=1;
    for (i = 0; i < G->numV; i++)
    {
        visited[i] = 0;            
        prev[i] = 0;             
        dist[i] = G->Edge[vs][i];
    }

    visited[vs] = 1;
    dist[vs] = 0;

    for (i = 1; i < G->numV; i++)
    {
        min = INF;
        for (j = 0; j < G->numV; j++)
        {
            if (visited[j]==0 && dist[j]<min)
            {
                min = dist[j];
                minid = j;
            }
        }
        visited[minid] = 1;

        for (j = 0; j < G->numV; j++)
        {
            tmp = (G->Edge[minid][j]==INF ? INF : (min + G->Edge[minid][j]));

            if (visited[j] == 0 && (tmp  < dist[j]) )
            {
                dist[j] = tmp;
                prev[j] = minid;
            }
        }
    }

    printf("dijkstra(%c): \n", G->Vertices[vs]);
    for (i = 0; i < G->numV; i++)
        printf("  shortest(%c, %c)=%d\n", G->Vertices[vs], G->Vertices[i], dist[i]);
}
int main()
{ 
    AdjMatrix G;
    freopen("1.txt","r",stdin);
    CreateGraph(&G);
    dijkstra(&G);
    DispGraph(G);

}

