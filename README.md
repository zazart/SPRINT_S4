# SPRINT_S4
Projet web dynamique, ITU Andoharanofotsy


///////////////////////////////////////////// A PROPOS /////////////////////////////////////////////
    
    FRAMEWORK WILK
            Le mot "wilk" est un mot polonais qui veut dire loup,
        Les loups sont souvent associés à des traits tels que la force, l'agilité, et la collaboration en meute.











///////////////////////////////////////////// FRONTCONTROLLER SERVLET /////////////////////////////////////////////

    Le développeur doit configurer dans le fichier xml du projet le FrontController
    Indiquer le package contenant ces fichiers annoté @Controller dans l'init-param
    
    Dans cette exemple le package est "mg.test.controller"

            <servlet>
                <servlet-name>FrontControllerServlet</servlet-name>
                <servlet-class>mg.itu.prom16.controller.FrontController</servlet-class>
                <init-param>
                    <param-name>controller-package</param-name>
                    <param-value>mg.test.controller</param-value>
                </init-param>
            </servlet>

            <servlet-mapping>
                <servlet-name>FrontControllerServlet</servlet-name>
                <url-pattern>/</url-pattern> 
            </servlet-mapping>










/////////////////////////////////////////////  RECUPERER LA CLASSE ET LA METHODE ASSOCIEES A UNE URL DONNEE  /////////////////////////////////////////////
    
    Annoter les méthodes des classes annoté @Controller par @Get("value") , remplacer "value" par la valeur de votre choix
            
    Nous avons la classe Controller_1 annoté @Controller et une méthode coucou() de cette classe annoté @Get("testCoucou")
    
            exemple de requête URL : "http://localhost:8080/wilk/testCoucou"

            resultat:
                chemin URL :    http://localhost:8080/wilk/testCoucou
                Mapping :       mg.test.controller.Controller_1
                MethodName :    coucou
            








/////////////////////////////////////////// MODE D'UTILISATION DE L'ANNOTATION @Get ///////////////////////////////////////////////

    L'annotation @Get est utilisé sur les méthodes des controlleurs , chaque méthode retournera un ModelView ou un String

    cas du ModelView :
        voici un exemple d'utilisation de l'annotation @Get
            
            @Get("link")
            public ModelView votreMethod() {
                ModelView mv = new ModelView("/destination.jsp");
                ....
                mv.addObject("votreClé", votreObjet);
                return mv;
            }

    remarques : 
      .L'url de la page destination doit toujours commencer par la racine du projet, donc si vous avez des sous dossiers contenant vos pages,  l'url doit ressembler à ceci ("/sousDossier/desination.jsp")
    
      .Utiliser la méthode addObject pour envoyer un Objet vers la page destination, récupérer la avec sa clé dans la page.


