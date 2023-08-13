class UseBeforeDeclaration {
    static {
        x = 100; 
        int v = ((x)) = 3; 
        int z = UseBeforeDeclaration.x * 2; 
        Object o = new Object(){
                void foo(){x++;} 
                {x++;} 
            };
    }
    {
        j = 200; 
        int n = j = 300; 
        int l = this.j * 3; 
        Object o = new Object(){
                void foo(){j++;} 
                { j = j + 1;} 
            };
    }
    int w = x= 3; 
    int p = x; 
    static int u = (new Object(){int bar(){return x;}}).bar(); 
    static int x;
    int m = j = 4; 
    int o = (new Object(){int bar(){return j;}}).bar(); 
    int j;
}
