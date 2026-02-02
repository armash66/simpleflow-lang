PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> function run($code) {
>>   (Invoke-WebRequest `
>>     -Uri "http://localhost:8080/run" `
>>     -Method POST `
>>     -Headers @{ "Content-Type" = "application/json" } `
>>     -Body (@{ code = $code } | ConvertTo-Json) `
>>     -UseBasicParsing
>>   ).Content | ConvertFrom-Json
>> }                                                                                                                                           PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set a = 10                                                         >> set b = 4                                                                                                                                   >> show a + b                                                                                                                                  
>> show a - b
>> show a * b
>> show a / b
>> exit"

output    error
------    -----
14...

                                                                                                                                               PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set a = 10                                                         >> set b = 4                                                                                                                                   >> show a + b                                                                                                                                  
>> show a - b
>> show a * b
>> show a / b
>> exit"

output    error
------    -----
14...


PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set a = 10                                                         >> set b = 4                                                                                                                                   >> show a + b                                                                                                                                  >> show a - b                                                                                                                                  
>> show a * b
>> show a / b
>> exit"

output    error
------    -----
14...


PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set x = 1                                                          >> show x                                                                                                                                      >> set x = x + 5                                                                                                                               >> show x                                                                                                                                      
>> exit"
>> 
        
output error
------ -----
1...

                                                                                                                                               PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set x = 3                                                          >> while (x > 0) {                                                                                                                             >>   show x                                                                                                                                    
>>   set x = x - 1
>> }
>> exit"
>>

output error
------ -----
3...        


PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set x = 5                                                          >> if (x > 3) {                                                                                                                                >>   show 1                                                                                                                                    >> } else {                                                                                                                                    
>>   show 0
>> }
>> exit"
>>

output                                     error
------                                     -----
Parse error at line 2: Expected statement.
                                                                                                                                                                                                                                                                                              PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "define add(a, b) {                                                 >>   return a + b                                                                                                                              
>> }
>> show add(2, 3)
>> exit"
>>

output error
------ -----
5

                                                                                                                                               PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "define square(x) {                                                 >>   return x * x                                                                                                                              >> }                                                                                                                                           
>> set n = 4
>> show square(n)
>> exit"
>>

output error                                                                                                                                   ------ -----                                                                                                                                   16                                                                                                                                                                                                                                                                                            

PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "set x =
>> exit"
>>

output                                      error
------                                      -----                                                                                              Parse error at line 2: Expected expression.                                                                                                                                                                                                                                                                                                                                                                                                  
PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> run "while (true) {
>>   show 1
>> }"
>>

output error
------ -----
       Execution limit exceeded


PS C:\Users\Armash Ansari\OneDrive\Desktop\Projects\Side Kicks\mylang> 