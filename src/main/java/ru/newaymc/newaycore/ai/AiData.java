package ru.newaymc.newaycore.ai;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class AiData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3774946055521111215L;

    private int state;
    private boolean allowAttack;
    private boolean seeTarget;

    private boolean baseMovement;
    private boolean findCover;
    private boolean borderPatrol;
    private boolean simpleFormation;
    /**
     * @param state 1 = NORMAL; 2 = ALERTED; 3 = IN BATTLE;
     * @param allowAttack
     * @param seeTarget
     * @param findCover
     * @param borderPatrol
     * @param simpleFormation
     */
    public AiData(int state, boolean allowAttack, boolean seeTarget, boolean baseMovement, boolean findCover, boolean borderPatrol, boolean simpleFormation) {
        this.state = state;
        this.allowAttack = allowAttack;
        this.seeTarget = seeTarget;
        this.baseMovement = baseMovement;
        this.findCover = findCover;
        this.borderPatrol = borderPatrol;
        this.simpleFormation = simpleFormation;
    }
}
