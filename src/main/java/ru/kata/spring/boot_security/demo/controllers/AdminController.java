package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping()
    public String showAllUsers(@ModelAttribute ("user") User user, Principal principal, Model model) {
        User authenticatedUser = userService.findByUsername(principal.getName()); //Нашли в БД юзера, который аутентифицировался

        //Добавляем в модель данные, чтобы отправить их на вьюшку admin-page
        model.addAttribute ("authenticatedUser", authenticatedUser); //Добавили самого юзера из БД
        model.addAttribute ("roleOfAuthenticatedUser", authenticatedUser.getRoles()); //Добавили его роли
        model.addAttribute("users", userService.findAll());// Добавили всех юзеров из БД
        model.addAttribute( "AllRoles", roleService.findAll()); //Добавили все роли из БД
        return "admin-page";
    }

    @GetMapping("/user-profile/{id}")
    public String findUser(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("AllRoles", user.getRoles());
        return "user-page";
    }

    /**
     * //Метод, выводящий форму для редактирования - edit
     * // Вьюшка "edit". Из нее данные отправляются на контроллер,
     * // т.е. на @RequestMapping("/admin"), а там на метод, принимающий PATCH-запросы.
     * // Таким будет метод - save_changes с аннотацией @PatchMapping
     * //В методе editUser помещаем в модель юзера с переданным из url-адреса id
     **/
    @GetMapping("/edit/{id}")
    public String editUser(Model model, @PathVariable("id") Long id) {
        model.addAttribute("user", userService.findUserById(id));
        model.addAttribute("AllRoles", roleService.findAll());
        return "redirect:/admin";
    }

    /**
     * //Метод, который принимает patch-запрос от вьюшки "edit"
     * // т.е. когда во view "edit" форму заполнили,
     * // нажали кнопку "Внести изменения", нас перебросит на url-адрес "/admin" на PATCH-метод
     * //@PatchMapping, потому что мы вносим при этом изменения, это заплатка сверху на то, что было
     * //Сюда придут новые данные юзера, которые хотят изменить
     * //И так, это метод сохраняющий изменения, которые пришли с формы метода editUser - это будет
     * // метод save_changes с аннотацией PATCH - @PatchMapping,
     * // Вьюшка будет редирект на url "/list-users"
     **/
    @PatchMapping("/update/{id}")
    public String updateUser(@ModelAttribute("user") User updateUser, @PathVariable("id") Long id) {
        userService.updateUser(updateUser, id); //Находим по id того юзера, которого надо изменить
        return "redirect:/admin";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUserById(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin";
    }


    /**
     * //Метод создания юзера - registration. Здесь хотим получить форму д/заполнения, поэтому аннотация с GET
     * // Будет появляться вьюшка "registration" для заполнения данных и потом перенаправлятся
     * // в контроллер на метод с аннотацией POST, поэтому на @RequestMapping("/admin"),
     * // а там на метод с аннотацией POST, т.е. @PostMapping(/create-user),
     * // А таким методом будет метод saveUser
     **/
    @GetMapping("/new")
    public String form_for_create_user(Model model, User user) {
        model.addAttribute("user", new User());
        List<Role> roles = (List<Role>) roleService.findAll();
        model.addAttribute("AllRoles", roles);
        return "redirect:/admin";
    }

    /**
     * //Метод сохранения только что добавленного юзера - saveNewUser
     * //Метод, принимающий POST-запросы, т.е. @PostMapping(), после выполнения будет редирект на вьюшку "/list-users"
     * //User user - это созданный юзер по html-форме, которая реализована во view "registration"
     * //Аннотация @ModelAttribute создаст юзера с теми значениями, которые придут из формы вьюшки "registration".
     * //Мы здесь принимаем в аргумент юзера, который пришел из заполненной формы
     * //Аннотация @ModelAttribute делает: создание нового объекта, добавление значений
     * //в поля этого объекта и затем добавление этого объекта в модель
     **/

    @PostMapping("/create")
    public String saveNewUser(@ModelAttribute("user") User user) {
        userService.saveUser(user); // Добавляем этого юзера в БД
        return "redirect:/admin";
    }
}
