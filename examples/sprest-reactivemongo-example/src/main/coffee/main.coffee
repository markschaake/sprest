console.info "we have coffee!"
window.myModule = angular.module "myModule", ["ngResource"]
window.ToDos = ($scope, $resource) ->
  ToDo = $resource('/api/todos/:id', id: '@id')

  $scope.todos = ToDo.query()
  $scope.addTodo = ->
    toAdd = new ToDo({text: $scope.todoText, done: false})
    toAdd.$save()
    $scope.todos.push toAdd
    $scope.todoText = null

  $scope.updateTodo = (todo) -> todo.$save()
  $scope.removeTodo = (todo) ->
    index = $scope.todos.indexOf(todo)
    todo.$remove()
    $scope.todos.splice(index, 1)

window.Reminders = ($scope, $resource) ->
  Reminder = $resource('/api/reminders/:id', id: '@id')

  $scope.reminders = Reminder.query()
  $scope.addReminder = ->
    toAdd = new Reminder({title: $scope.reminderText, remindAt: new Date().getTime()})
    toAdd.$save()
    $scope.reminders.push toAdd
    $scope.reminderText = null

  $scope.updateReminder = (reminder) -> reminder.$save()
  $scope.removeReminder = (reminder) ->
    index = $scope.reminders.indexOf(reminder)
    reminder.$remove()
    $scope.reminders.splice(index, 1)
