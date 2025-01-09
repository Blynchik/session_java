package ru.service.session.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.service.session.dto.event.internal.DecisionType;
import ru.service.session.dto.event.request.DecisionRequest;
import ru.service.session.dto.event.request.DecisionResultRequest;
import ru.service.session.dto.event.request.EventRequest;
import ru.service.session.dto.hero.HeroRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ObjectFactory {

    public static String getRandomString(Random random, int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            string.append(Character.toString('A' + random.nextInt(26)));
        }
        return string.toString();
    }

    public static <T> T convertJsonToObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Convert error " + clazz.getSimpleName(), e);
        }
    }

    public static <T> List<T> convertJsonToList(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("Convert error " + clazz.getSimpleName() + ">", e);
        }
    }

    public static String getChangedSignToken() {
        return getEternalToken().subSequence(0, getEternalToken().length() - 2) + "A";
    }

    public static String getExpiredToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiaWJhQHlhbmRleC5ydSIsInVzZXJJZCI6MSwiaWF0IjoxNzMyMTk3NDAyLCJleHAiOjE3MzIxOTc0MTIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdfQ.Uw76kNVdU-mK_YB59xLMCtKZjScKEzyeolxR610jlyA";
    }

    public static String getEternalToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiaWJhQHlhbmRleC5ydSIsInVzZXJJZCI6MSwiaWF0IjoxNzMyMTk3ODgzLCJleHAiOjIwMzk3ODE4ODMsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdfQ.g9KlcaBmOD1qMH4P0Gq_-Dn_GVcsX14EFzYvtfYXAZI";
    }

    public static String getEternalAdminToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiaWJhQHlhbmRleC5ydSIsInVzZXJJZCI6MSwiaWF0IjoxNzM0NDQwNzAyLCJleHAiOjIwNDk4MDA3MDIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiIsIlJPTEVfQURNSU4iXX0.l_RFfhdX5boOLI5AkShItkzrGlYCSRmguUyND0L3zfA";
    }

    public static HeroRequest getHeroRequest3() {
        return getHeroRequestCustom("Сияющий", "Рыцарь", 130, 80, 100, 100, 90, 100);
    }

    public static HeroRequest getHeroRequest2() {
        return getHeroRequestCustom("Сияющий", "Рыцарь", 100, 80, 100, 100, 90, 130);
    }

    public static HeroRequest getHeroRequest1() {
        return getHeroRequestCustom("Сияющий", "Рыцарь", 100, 100, 100, 100, 100, 100);
    }

    public static HeroRequest getHeroRequestCustom(String name, String lastname,
                                                   Integer str, Integer dex, Integer con, Integer intl, Integer wis, Integer cha) {
        return new HeroRequest(name, lastname, str, dex, con, intl, wis, cha);
    }

    public static EventRequest getEventRequest5() {
        return getEventRequestCustom("Ужасный дракон!",
                "Вам встретился ужасный дракон! Как вы поступите?",
                new ArrayList<>() {{
                    add(getDecisionRequestCustom(DecisionType.DEX,
                            "Не привлекая внимания, быстро скроюсь, пока дракон меня не заметил",
                            6,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы ушли от дракона"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.DEX,
                            "Спрячусь и останусь неподвижным, пока дракон не уйдет",
                            6,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Дракон ушел"),
                                    false, new DecisionResultRequest("Дракон слопал вас"))));
                }}
        );
    }

    public static EventRequest getEventRequest4() {
        return getEventRequestCustom("Ужасный дракон!",
                "Вам встретился ужасный дракон! Как вы поступите?",
                new ArrayList<>() {{
                    add(getDecisionRequestCustom(DecisionType.TEXT,
                            "Не привлекая внимания, быстро скроюсь, пока дракон меня не заметил",
                            0,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы ушли от дракона"),
                                    false, new DecisionResultRequest("Вы ушли от дракона"))));
                    add(getDecisionRequestCustom(DecisionType.TEXT,
                            "Спрячусь и останусь неподвижным, пока дракон не уйдет",
                            0,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Дракон ушел"),
                                    false, new DecisionResultRequest("Дракон ушел"))));
                }}
        );
    }

    public static EventRequest getEventRequest3() {
        return getEventRequestCustom("Ужасный дракон!",
                "Вам встретился ужасный дракон! Как вы поступите?",
                new ArrayList<>() {{
                    add(getDecisionRequestCustom(DecisionType.STR,
                            "Поборю дракона своей силой",
                            10,
                            new ArrayList<>() {{
                                add("Бью дракона");
                                add("Кусаю дракона");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы победили дракона"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.STR,
                            "Столкну на дракона скалу",
                            8,
                            new ArrayList<>() {{
                                add("Толкаю скалу");
                                add("Раскачиваю скалу");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы придавили дракона скалой"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.CHA,
                            "Заболтаю дракона",
                            8,
                            new ArrayList<>() {{
                                add("Льщу дракону");
                                add("Заговариваю зубы дракону");
                            }},
                            Map.of(true, new DecisionResultRequest("Дракон наградил вас за сладкие речи"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.TEXT,
                            "Не привлекая внимания, быстро скроюсь, пока дракон меня не заметил",
                            0,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы ушли от дракона"),
                                    false, new DecisionResultRequest("Вы ушли от дракона"))));
                }}
        );
    }

    public static EventRequest getEventRequest2() {
        return getEventRequestCustom("Ужасный дракон!",
                "Вам встретился ужасный дракон! Как вы поступите?",
                new ArrayList<>() {{
                    add(getDecisionRequestCustom(DecisionType.DEX,
                            "Прокрадусь и украду все сокровища дракона",
                            10,
                            new ArrayList<>() {{
                                add("На цыпочках двигаюсь к сокровищам");
                                add("Стараюсь не издавать звуков");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы украли все сокровища дракона"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.DEX,
                            "Найду уязвимое место дракона и убью",
                            8,
                            new ArrayList<>() {{
                                add("Внимательно высматриваю брешь в шкуре дракона");
                                add("Ищу слабое место дракона");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы проткнули брюхо дракона насквозь"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.DEX,
                            "Не привлекая внимания, быстро скроюсь, пока дракон меня не заметил",
                            6,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы ушли от дракона"),
                                    false, new DecisionResultRequest("Вы ушли от дракона"))));
                }}
        );
    }

    public static EventRequest getEventRequest1() {
        return getEventRequestCustom("Ужасный дракон!",
                "Вам встретился ужасный дракон! Как вы поступите?",
                new ArrayList<>() {{
                    add(getDecisionRequestCustom(DecisionType.STR,
                            "Поборю дракона своей силой",
                            10,
                            new ArrayList<>() {{
                                add("Бью дракона");
                                add("Кусаю дракона");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы победили дракона"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.CHA,
                            "Заболтаю дракона",
                            8,
                            new ArrayList<>() {{
                                add("Льщу дракону");
                                add("Заговариваю зубы дракону");
                            }},
                            Map.of(true, new DecisionResultRequest("Дракон наградил вас за сладкие речи"),
                                    false, new DecisionResultRequest("Дракон вас слопал"))));
                    add(getDecisionRequestCustom(DecisionType.TEXT,
                            "Не привлекая внимания, быстро скроюсь, пока дракон меня не заметил",
                            0,
                            new ArrayList<>() {{
                                add("На цыпочках отхожу от дракона");
                                add("Тихо-тихо скрываюсь в тенях");
                            }},
                            Map.of(true, new DecisionResultRequest("Вы ушли от дракона"),
                                    false, new DecisionResultRequest("Вы ушли от дракона"))));
                }}
        );
    }

    public static EventRequest getEventRequestCustom(String title, String description,
                                                     List<DecisionRequest> decisions) {
        return new EventRequest(title, description, decisions);
    }

    public static DecisionRequest getDecisionRequestCustom(DecisionType decisionType, String description, int difficulty,
                                                           List<String> decisionLog,
                                                           Map<Boolean, DecisionResultRequest> results) {
        return new DecisionRequest(decisionType.name(), description, decisionLog, difficulty, results);
    }

    public static DecisionResultRequest getDecisionResultRequestCustom(String resultDescr) {
        return new DecisionResultRequest(resultDescr);
    }
}
