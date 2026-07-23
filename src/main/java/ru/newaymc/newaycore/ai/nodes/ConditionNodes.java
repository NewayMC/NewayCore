package ru.newaymc.newaycore.ai.nodes;

import ru.newaymc.newaycore.ai.ShooterCore;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Node;
import ru.newaymc.newaycore.ai.utils.Status;

public class ConditionNodes {
    public static class IsHealthLowNode extends Node {
        private final int threshold;

        public IsHealthLowNode(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public Status tick(Memory memory) {
            return memory.getMob().getHealth() < threshold ? Status.SUCCESS : Status.FAILURE;
        }
    }

    @Deprecated
    public static class HasAmmoNode extends Node {

        @Override
        public Status tick(Memory memory) {
            return Status.FAILURE; //ShooterCore.getAmmo() > 5 ? Status.SUCCESS : Status.FAILURE;
        }
    }

    public static class IsInCoverNode extends Node {

        @Override
        public Status tick(Memory memory) {
            if (memory.getCurrentCover() != null) {
                return Status.SUCCESS;
            } else {
                return Status.FAILURE;
            }
        }
    }

    public static class HasTarget extends Node {

        @Override
        public Status tick(Memory memory) {
            return memory.getTarget() != null ? Status.SUCCESS : Status.FAILURE;
        }
    }
}
