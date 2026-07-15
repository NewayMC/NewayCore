package ru.newaymc.newaycore.ai.nodes;

import lombok.Getter;
import ru.newaymc.newaycore.ai.objects.Memory;
import ru.newaymc.newaycore.ai.utils.Interruptible;
import ru.newaymc.newaycore.ai.utils.Node;
import ru.newaymc.newaycore.ai.utils.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Composite {
    public static class ConditionNode extends Node {
        private Predicate<Memory> predicate;

        public ConditionNode(Predicate<Memory> pred) {
            this.predicate = pred;
        }

        @Override
        public Status tick(Memory memory) {
            return predicate.test(memory) ? Status.SUCCESS : Status.FAILURE;
        }
    }

    public static class SequenceNode extends Node {
        @Getter
        private final List<Node> children;
        private int currentChildIndex = 0;

        public SequenceNode(Node... nodes) {
            this.children = Arrays.asList(nodes);
        }

        @Override
        public Status tick(Memory memory) {
            for (int i = currentChildIndex; i < children.size(); i++) {
                Status status = children.get(i).tick(memory);

                if (status == Status.RUNNING) {
                    currentChildIndex = i;
                    return Status.RUNNING;
                }

                if (status == Status.FAILURE) {
                    currentChildIndex = 0;
                    return Status.FAILURE;
                }
            }
            currentChildIndex = 0;
            return Status.SUCCESS;
        }
    }

    public static class AbortIfNode extends Node {
        private Node condition;
        private Node child;
        private boolean wasRunning = false;

        public AbortIfNode(Node condition, Node child) {
            this.condition = condition;
            this.child = child;
        }

        @Override
        public Status tick(Memory memory) {
            Status conditionStatus = condition.tick(memory);

            if (conditionStatus == Status.SUCCESS) {
                if (wasRunning) {
                    if (child instanceof Interruptible) {
                        ((Interruptible) child).onInterrupt(memory);
                    }
                    wasRunning = false;
                }
                return Status.FAILURE;
            }

            Status result = child.tick(memory);

            if (result == Status.RUNNING) {
                wasRunning = true;
            } else {
                wasRunning = false;
            }

            return result;
        }
    }

    public static class SelectorNode extends Node {
        private List<Node> children = new ArrayList<>();
        private int currentChildIndex = 0;

        public SelectorNode(Node... nodes) {
            this.children = Arrays.asList(nodes);
        }

        @Override
        public Status tick(Memory memory) {
            for (int i = currentChildIndex; i < children.size(); i++) {
                Status status = children.get(i).tick(memory);

                if (status == Status.RUNNING) {
                    currentChildIndex = i;
                    return Status.RUNNING;
                }

                if (status == Status.SUCCESS) {
                    currentChildIndex = 0;
                    return Status.SUCCESS;
                }
            }
            currentChildIndex = 0;
            return Status.FAILURE;
        }
    }

    public static class ActiveSelectorNode extends Node {
        private List<Node> children =  new ArrayList<>();
        private Node currentChild = null;
        private int currentIndex = -1;

        public ActiveSelectorNode(Node... nodes) {
            this.children = Arrays.asList(nodes);
        }

        @Override
        public Status tick(Memory memory) {
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);

                if (i == currentIndex && currentChild != null) {
                    continue;
                }

                Status checkStatus = quickCheck(child, memory);

                if (checkStatus == Status.SUCCESS) {
                    if (currentChild != null && currentChild instanceof Interruptible) {
                        ((Interruptible) currentChild).onInterrupt(memory);
                    }

                    currentIndex = i;
                    currentChild = child;
                    return child.tick(memory);
                }
            }

            if (currentChild != null) {
                Status result = currentChild.tick(memory);
                if (result != Status.RUNNING) {
                    currentChild = null;
                    currentIndex = -1;
                }
                return result;
            }

            return Status.FAILURE;
        }

        private Status quickCheck(Node node, Memory memory) {
            if (node instanceof SequenceNode) {
                SequenceNode seq = (SequenceNode) node;
                List<Node> children = seq.getChildren();
                if (!children.isEmpty()) {
                    return children.get(0).tick(memory);
                }
            }

            if (node instanceof AbortIfNode) {
                return Status.FAILURE;
            }

            return node.tick(memory);
        }
    }
}
