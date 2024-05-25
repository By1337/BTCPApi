package org.by1337.tcpapi.server.addon;

import com.google.common.base.Joiner;
import junit.framework.TestCase;

import java.util.*;

public class AddonInitializerTest extends TestCase {
    public void test() {
        AddonDescriptionFile addon1 = new AddonDescriptionFile("A", "main", "1.0", "desc", Collections.emptySet(), Set.of("B"), Collections.emptySet());
        AddonDescriptionFile addon2 = new AddonDescriptionFile("B", "main", "1.0", "desc", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        AddonDescriptionFile addon3 = new AddonDescriptionFile("C", "main", "1.0", "desc", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        AddonDescriptionFile addon4 = new AddonDescriptionFile("D", "main", "1.0", "desc", Collections.emptySet(), Set.of("A"), Collections.emptySet());
        AddonDescriptionFile addon5 = new AddonDescriptionFile("E", "main", "1.0", "desc", Collections.emptySet(), Set.of("C"), Collections.emptySet());
        AddonDescriptionFile addon6 = new AddonDescriptionFile("F", "main", "1.0", "desc", Collections.emptySet(), Set.of("B"), Collections.emptySet());

        AddonDescriptionFile addon7 = new AddonDescriptionFile("G", "main", "1.0", "desc", Collections.emptySet(), Set.of("K"), Collections.emptySet());
        AddonDescriptionFile addon8 = new AddonDescriptionFile("H", "main", "1.0", "desc", Collections.emptySet(), Set.of("G"), Collections.emptySet());
        AddonDescriptionFile addon9 = new AddonDescriptionFile("K", "main", "1.0", "desc", Collections.emptySet(), Set.of("H"), Collections.emptySet());

        List<WeightedItem<AddonDescriptionFile>> list =
                new ArrayList<>(List.of(
                        new WeightedItem<>(addon9),
                        new WeightedItem<>(addon8),
                        new WeightedItem<>(addon7),
                        new WeightedItem<>(addon6),
                        new WeightedItem<>(addon5),
                        new WeightedItem<>(addon4),
                        new WeightedItem<>(addon3),
                        new WeightedItem<>(addon2),
                        new WeightedItem<>(addon1)
                ));

        Collections.shuffle(list);

        Map<String, WeightedItem<AddonDescriptionFile>> lookup = new HashMap<>();
        for (WeightedItem<AddonDescriptionFile> item : list) {
            lookup.put(item.val.getName(), item);
        }
        boolean hasChange;
        int x = 0;
        do {
            x++;
            hasChange = false;
            main:
            for (WeightedItem<AddonDescriptionFile> item : list) {
                for (String string : item.val.getDepend()) {
                    var v = lookup.get(string);
                    if (item.weight <= v.weight) {
                        hasChange = true;
                        item.weight = v.weight + 1;
                        break main;
                    }
                }
            }
            if (x > 2_000) {
                List<WeightedItem<AddonDescriptionFile>> error = new ArrayList<>();
                for (WeightedItem<AddonDescriptionFile> item : new ArrayList<>(list)) {
                    if (item.weight > 1_900) {
                        error.add(item);
                        list.removeIf(i -> i == item);
                    }
                }
                System.err.println(
                        "Обнаружена циклическая зависимость между [" + Joiner.on(", ").join(
                                error.stream().map(i -> String.format("{%s-%s %s, %s}", i.val.getName(), i.val.getVersion(), i.val.getDepend(), i.val.getSoftDepend())).toList()
                        )
                );
                x = 0;
            }
        } while (hasChange);

        list.sort(Comparator.comparingInt(o -> o.weight));

        System.out.println(
                Joiner.on(" -> ").join(list.stream().map(i -> i.val.getName()).toList())
        );

        Set<String> loaded = new HashSet<>();

        for (WeightedItem<AddonDescriptionFile> addon : list) {
            // Проверяем, что все обязательные зависимости аддона уже загружены
            if (!loaded.containsAll(addon.val.getDepend())) {
                throw new IllegalStateException(
                        "Аддон " + addon.val.getName() + " не может запуститься без зависимостей " + Joiner.on(", ").join(addon.val.getDepend()) + ". Загружено: " + Joiner.on(", ").join(loaded)
                );
            }

            // Если аддон имеет обязательные зависимости, добавляем их в список загруженных
            loaded.add(addon.val.getName());
        }

    }

    private static class WeightedItem<T> {
        private int weight = 0;
        private final T val;

        public WeightedItem(T val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return "WeightedItem{" +
                    "weight=" + weight +
                    ", val=" + val +
                    '}';
        }
    }


}