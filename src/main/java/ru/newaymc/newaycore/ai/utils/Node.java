package ru.newaymc.newaycore.ai.utils;

import ru.newaymc.newaycore.ai.objects.Memory;

public abstract class Node {
    public abstract Status tick(Memory memory);
}
