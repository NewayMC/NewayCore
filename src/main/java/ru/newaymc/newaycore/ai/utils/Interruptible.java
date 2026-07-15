package ru.newaymc.newaycore.ai.utils;

import ru.newaymc.newaycore.ai.objects.Memory;

public interface Interruptible {
    void onInterrupt(Memory memory);
}
