public class BadBinaryLiterals {
    int valid = 0b0;            
    int baddigit = 0b012;       
    int overflow1 = 0b111111111111111111111111111111111; 
    int overflow2 = 0b11111111111111111111111111111111111111111111111111111111111111111L; 
    float badfloat1 = 0b01.01;  
    float badfloat2 = 0b01e01;  
}
