package org.by1337.tcpapi.server.addon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class AddonDescriptionFile {
    public static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private final String name;
    private final String mainClass;
    private final String version;
    private final String description;
    private final Set<String> authors;
    private final Set<String> depend;
    private final Set<String> softDepend;

    AddonDescriptionFile(String name, String mainClass, String version, String description, Set<String> authors, Set<String> depend, Set<String> softDepend) {
        this.name = name;
        this.mainClass = mainClass;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.depend = depend;
        this.softDepend = softDepend;
    }

    public AddonDescriptionFile(JsonObject object) {
        name = object.get("name").getAsString();
        validate(name);
        mainClass = object.get("main").getAsString();
        if (object.has("description")) {
            description = object.get("description").getAsString();
        } else {
            description = "";
        }
        version = object.get("version").getAsString();
        if (object.has("authors")) {
            authors = toSetString(object.getAsJsonArray("authors"));
        } else {
            authors = new HashSet<>();
        }
        if (object.has("author")) {
            authors.add(object.get("author").getAsString());
        }
        if (object.has("depend")) {
            depend = toSetString(object.getAsJsonArray("depend"));
        } else {
            depend = new HashSet<>();
        }
        if (object.has("soft-depend")) {
            softDepend = toSetString(object.getAsJsonArray("soft-depend"));
        } else {
            softDepend = new HashSet<>();
        }

    }

    public AddonDescriptionFile(String json) {
        this(new Gson().fromJson(json, JsonObject.class));
    }

    private Set<String> toSetString(JsonArray array) {
        Set<String> list = new HashSet<>(array.size());
        for (JsonElement jsonElement : array) {
            list.add(jsonElement.getAsString());
        }
        return list;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getMain() {
        return mainClass;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public static void validate(String input) {
        validate(input, () -> String.format("Invalid name. Must be [a-zA-Z0-9._-]: '%s'", input));
    }

    public static void validate(String input, Supplier<String> message) {
        if (!pattern.matcher(input).matches()) {
            throw new IllegalArgumentException(message.get());
        }
    }

    public Set<String> getDepend() {
        return depend;
    }

    public Set<String> getSoftDepend() {
        return softDepend;
    }
}
