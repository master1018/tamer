public class RegisterAllocation {
    private Set<RegisterAllocation> collisions = new HashSet<RegisterAllocation>();
    Set<Instruction> references = new HashSet<Instruction>();
}
