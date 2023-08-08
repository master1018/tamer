public class UserController {
    @Resource
    UserService userService;
    @RequestMapping("/list.do")
    String getListOfUsers(Model model, UserSearchCriteria searchCriteria) {
        List<User> usersList = userService.getUsers(searchCriteria);
        model.addAttribute("usersList", usersList);
        return "users/list";
    }
    @RequestMapping("/create.do")
    String createUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "users/create";
    }
    @RequestMapping("/save.do")
    String saveUser(User user) {
        userService.save(user);
        return "redirect:/users/list.do";
    }
}
