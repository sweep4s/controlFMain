necesitas java 21

primero crear el archivo .env
tienes el .env.template necesitas una api key de gemini

ir con `controlFf_frontend` ir correr
```bash
npm run build
```
se creara una carpeta llamada dist,
mover el contenido dentro de `src/main/resources/static`
la carpeta debe estar asi
```bash
Mode                 LastWriteTime         Length Name                                                                                                      
----                 -------------         ------ ----                                                                                                      
d-----         7/12/2026   9:14 AM                assets                                                                                                    
-a----         7/12/2026   9:14 AM           9522 favicon.svg                                                                                               
-a----         7/12/2026   9:14 AM           5055 icons.svg                                                                                                 
-a----         7/12/2026   9:14 AM            480 index.html
```

```bash
cd controlF
./gradlew build -x tes
cd ..
docker-compose up
```

para reconstruir todo
```bash
docker-compose up --build
```

#credenciales de administrador
```markdown
user: admin@controlf.dev 
password: DevAdmin2026!
```


