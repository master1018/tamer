public class AddOwnerForm {
    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(Model model) {
        Owner owner = new Owner();
        model.addAttribute(owner);
        return "ownerForm";
    }
    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("owner") Owner owner, BindingResult result, SessionStatus status) {
        if (OwnerValidator.validate(owner, result).hasErrors()) {
            return "ownerForm";
        } else {
            Owner merged = owner.merge().flush();
            owner.setId(merged.getId());
            status.setComplete();
            return "redirect:owner.do?ownerId=" + owner.getId();
        }
    }
}
