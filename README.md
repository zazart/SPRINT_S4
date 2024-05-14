# SPRINT_S4
Projet web dynamique, ITU Andoharanofotsy


///////////////////////////////////////////// A PROPOS /////////////////////////////////////////////
    
    FRAMEWORK WILK
            Le mot "wilk" est un mot polonais qui veut dire loup,
        Les loups sont souvent associés à des traits tels que la force, l'agilité, et la collaboration en meute.





///////////////////////////////////////////// FRONTCONTROLLER SERVLET /////////////////////////////////////////////

    Le développeur doit configurer dans le fichier xml du projet le FrontServlet 
    Indiquer le package contenant ces fichiers annoté @Controller dans l'init-param

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







