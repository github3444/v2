package xyz.sprov.blog.sprovui.util;

import spark.ModelAndView;
import spark.Request;
import spark.Route;
import spark.TemplateEngine;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class SparkUtil {

    private static final TemplateEngine templateEngine;

    static {
//        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//        templateResolver.setCharacterEncoding("UTF-8");
//        templateResolver.setTemplateMode(TemplateMode.HTML);
//        templateResolver.setPrefix("templates/");
//        templateResolver.setSuffix(".html");
//        templateResolver.setCacheTTLMs(3600000L);
//
//        templateEngine = new ThymeleafTemplateEngine(templateResolver);

        templateEngine = new VelocityTemplateEngine("UTF-8");
    }

    private SparkUtil() {}

    public static boolean isAjax(Request request) {
        return "XMLHttpRequest".equals(request.headers("X-Requested-With"));
    }

    public static String render(String path) {
        return render(new HashMap<>(), path);
    }

    public static String render(Map<String, Object> model, String path) {
        if (model == null) {
            model = new HashMap<>();
        }
        return templateEngine.render(new ModelAndView(model, "/templates" + path + ".html"));
    }

    public static Route view(String path) {
        return (request, response) -> render(path);
    }

}
